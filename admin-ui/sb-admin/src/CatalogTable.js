import React, { Component } from "react";
import dompurify from "dompurify";

class CatalogTable extends Component {
  defaultPageSize = 10;
  activePages = {};
  beadcrumbs;
  constructor(props) {
    super(props);
    this.state = {
      contentId: props.contentId,
      history: props.history,
      parentEntity: null,
      entitiesBylayouts: {},
      loaded: false
    };
    this.breadcrumbs = [];
    this.rowTapped = this
      .rowTapped
      .bind(this);
    this.handleBreadCrumbClicked = this
      .handleBreadCrumbClicked
      .bind(this);
    this.confirmDeleteElement = this.confirmDeleteElement.bind(this);
    this.editElement = this.editElement.bind(this);
    this.getPaginationInfo = this.getPaginationInfo.bind(this);
  }

  componentDidMount() {
    if (!this.state.loaded) {
      this.getData(this.state.contentId);
    }
  }

  rowTapped(contentId) {
    this.state.loaded = false;
    this.state.history('/apicatalog/' + contentId);
    this.state.contentId = contentId;
    this.getData(this.state.contentId);
  }

  handleBreadCrumbClicked(contentId) {
    console.log("breadcrumb clicked");
    this.state.loaded = false;
    this.state.history('/apicatalog/' + contentId);
    this.getData(contentId);
  }

  confirmDeleteElement(contentId) {
    if (window.confirm('Are you sure you want to delete element ' + contentId + '?')) {
      // delete

    }
  }

  editElement(contentId) {
    // edit element
  }

  sortEntitiesByLayouts(data) {
    let entitiesBylayouts = {};
    for (let layout in data.layouts) {
      if (entitiesBylayouts[data.layouts[layout].name] == null) {
        var layoutWithEntities = {
          layout: data.layouts[layout],
          entities: []
        };
        entitiesBylayouts[data.layouts[layout].name] = layoutWithEntities;
      }
    }

    for (let entity in data.entities) {
      if (data.entities[entity].id != this.state.contentId) {
        entitiesBylayouts[data.entities[entity].layoutName].entities.push(data.entities[entity]);
      }
    }

    return entitiesBylayouts
  }

  getBreadCrumbName() {
    if (this.state.contentId == 0) {
      return "root";
    }
    if (this.state.parentEntity == null) {
      return "";
    }
    if (this.state.parentLayout == null || this.state.parentLayout.breadCrumbElementName == null) {
      return this.state.parentEntity.id;
    } else {
      return this.state.parentEntity[this.state.parentLayout.breadCrumbElementName];
    }
  }

  getData(id) {
    if (!this.state.loaded) {
      if (id == null) {
        id = 0;
      }
      // todo link from configuration
      fetch("http://localhost:8080/catalog/" + id + "?children=true&parents=true")
        .then(res => res.json())
        .then(
          (result) => {
            console.log("Received set of " + result.entities.length + " objects");
            var entitiesBylayouts = this.sortEntitiesByLayouts(result);
            if (Object.keys(this.activePages).length == 0) {
              for (var entityByLayoutIndex in entitiesBylayouts) {
                this.activePages[entityByLayoutIndex] = { currentPage: 0, elements: entitiesBylayouts[entityByLayoutIndex].entities };
              }
            }
            // todo use method
            let parent = result.entities.filter(entity => this.state.contentId == entity.id)[0];
            let parentLayout;
            if (parent) {
              let parentLayouts = result.layouts.filter(layout => layout.name == parent.layoutName);
              if (parentLayouts.length == 0) {
                parentLayout = null;
              } else {
                parentLayout = parentLayouts[0];
              }
            }
            this.breadcrumbs = result.parents.reverse();

            this.setState({
              entitiesBylayouts: entitiesBylayouts,
              parentEntity: parent,
              parentLayout: parentLayout,
              loaded: true
            });
          },
          (error) => {
            console.log("Error during request: " + error);
          }
        )
    }
  }

  getPageFirstElementIndex(layoutName) {
    return this.activePages[layoutName].currentPage * this.defaultPageSize + 1;
  }

  getPaginationInfo(layoutName) {
    let wholeNumberOfRecordsByLayout = this.state.entitiesBylayouts[layoutName].entities.length;
    let pageLastElementIndex = this.activePages[layoutName].currentPage * this.defaultPageSize + this.defaultPageSize;
    let elementsCount = pageLastElementIndex <= wholeNumberOfRecordsByLayout 
    ? pageLastElementIndex
    : wholeNumberOfRecordsByLayout;
    return (`Showing ${this.getPageFirstElementIndex(layoutName)} to ${elementsCount} of ${wholeNumberOfRecordsByLayout} entries`);
  }

  renderParentElement() {
    if (this.state.parentEntity == null || this.state.parentLayout == null) {
      return (<div></div>);
    }
    
    const sanitizer = dompurify.sanitize;
    let htmlString = this.state.parentLayout.html;
    for (var element in Object.keys(this.state.parentEntity)) {
      var key = Object.keys(this.state.parentEntity)[element];
      htmlString = htmlString.replaceAll("{{" + key + "}}", this.state.parentEntity[key]);
    }
    return (
      <div dangerouslySetInnerHTML={{ __html: 
        sanitizer(htmlString) 
      }} />
    )
  }

  /**
   * 
   * @param {*} item 
   * @param {*} attribute e.g. TEXT, IMAGE, FILE, HTML, BOOLEAN, ENUM, ARRAY
   * @returns 
   */
  renderTableItem(item, attribute) {
    if (attribute.resolve == "TEXT") {
      return item;
    } else if (attribute.resolve == "IMAGE") {
      return <img src={item}></img>
    }
  }

  renderBreadCrumbs() {
    return (<ol className="breadcrumb mb-4">
      <li className="breadcrumb-item">
        {/*  todo root element for current user */}
        <a href="/apicatalog/0">Root</a></li>
      {Object.values(this.breadcrumbs).map(breadcrumb => (<li onClick={() => this.handleBreadCrumbClicked(breadcrumb.id)} className="breadcrumb-item active">{breadcrumb.stringValue}</li>))}

    </ol>);
  }

  getSubList(tableElements) {
    let result = [];
    for (let index = this.activePages[tableElements.layout.name].currentPage * this.defaultPageSize; 
      index < (this.activePages[tableElements.layout.name].currentPage  + 1)* this.defaultPageSize;
      index++) {
        if (tableElements.entities.length > index) {
          result.push(tableElements.entities[index]);
        }
      }
    return result;
  }

  setCurrentPage(layoutName, index) {
    console.log("[DEBUG] Setting page index = " + index + " for layout " + layoutName);
    // todo active pages to state
    if (this.activePages[layoutName].currentPage != parseInt(index)) {
      this.activePages[layoutName].currentPage = parseInt(index);
      this.setState(this.state);
    }
  }

  renderTablePagination(tableElements) {
    var numberOfPages = Math.ceil(tableElements.entities.length / this.defaultPageSize);
    var elements = [];
    var selectElement = [];
    // very strange way to get element indexes
    for (var index = 0; index < numberOfPages; index++) {
      selectElement.push(index);
    }
    Object.keys(selectElement).map(selectElement => {
      var className = this.activePages[tableElements.layout.name].currentPage == selectElement
      ? "active"
      : "";

      elements.push(
      <li className={className}>
        <a id={selectElement} onClick={() => this.setCurrentPage(tableElements.layout.name, selectElement)}>{parseInt(selectElement) + 1}</a>
      </li>
      );
    }
    )
    return (<ul className="dataTable-pagination-list">{elements}</ul>);
    
  }

  renderTable(tableElements) {
    if (tableElements.layout.attributes == null || tableElements.entities.length == 0) {
      return (<div></div>);
    }
    return (
      <div>
        <p>{tableElements.layout.name}</p>
        <table id={tableElements.layout.name} className="dataTable-table">
          <thead>
            <tr key={"header"}>
              {Object.values(tableElements.layout.attributes).map((key) => (
                <th>{key.name}</th>
              ))}
              <th style={{ padding: '0.375rem 0.5rem', width: '6em' }}>
                <a href="#" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.75rem!important', fontStyle: 'normal', padding: '0.375rem 0.5rem', borderColor: '#0d6efd', backgroundColor: '#0d6efd', color: '#fff', borderRadius: '0.375rem' }}>
                  <i className="fa fa-plus-square"></i>
                </a>
              </th>
            </tr>
          </thead>
          <tbody>
            {this.getSubList(tableElements)
              .map((item) => (
              <tr id={"item-" + item.id} key={item.id}>
                {Object.values(tableElements.layout.attributes).map((attribute) => (
                  <td onClick={() => this.rowTapped(item.id)}>{this.renderTableItem(item[attribute.key], attribute)}</td>
                ))}
                <td style={{ display: 'flex', padding: '0.375rem 0.5rem', width: '6em' }}>
                  <a href="#" className="fa-thin fa-pen-to-square" style={{ display: 'flex', width: '2.5em', alignItems: 'center', justifyContent: 'center', fontSize: '1.75rem!important', padding: '0.375rem 0.5rem', marginRight: '0.1em', borderColor: '#0d6efd', backgroundColor: '#0d6efd', color: '#fff', borderRadius: '0.375rem' }}
                  >
                    <i className="far fa-edit"></i>
                  </a>
                  <a style={{ display: 'flex', width: '2.5em', alignItems: 'center', justifyContent: 'center', fontSize: '1.75rem!important', padding: '0.375rem 0.5rem', marginLeft: '0.1em', borderColor: '#0d6efd', backgroundColor: '#0d6efd', color: '#fff', borderRadius: '0.375rem' }}>
                    <i onClick={() => this.confirmDeleteElement(item.id)} className="far fa-trash-alt"></i>
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="dataTable-bottom">
          <div className="dataTable-info">{this.getPaginationInfo(tableElements.layout.name)}</div>
          <nav className="dataTable-pagination">
              {this.renderTablePagination(tableElements)}
              {/* <li className="active"><a href="#" data-page="1">1</a></li>
              <li className=""><a href="#" data-page="2">2</a></li>
              <li className=""><a href="#" data-page="3">3</a></li>
              <li className=""><a href="#" data-page="4">4</a></li>
              <li className=""><a href="#" data-page="5">5</a></li>
              <li className=""><a href="#" data-page="6">6</a></li>
              <li className="pager"><a href="#" data-page="2">›</a></li> */}
            {/* </ul> */}
          </nav>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div className="container-fluid px-4">
        <h1 className="mt-4">Catalog</h1>
        {this.renderBreadCrumbs()}
        {this.renderParentElement()}
        <div className="card mb-4">
          <div className="card-header">
            <svg className="svg-inline--fa fa-table fa-w-16 me-1" aria-hidden="true" focusable="false" data-prefix="fas" data-icon="table" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" data-fa-i2svg="">
              <path fill="currentColor" d="M464 32H48C21.49 32 0 53.49 0 80v352c0 26.51 21.49 48 48 48h416c26.51 0 48-21.49 48-48V80c0-26.51-21.49-48-48-48zM224 416H64v-96h160v96zm0-160H64v-96h160v96zm224 160H288v-96h160v96zm0-160H288v-96h160v96z"></path>
            </svg>
            Child records
          </div>
          <div className="card-body">
            <div className="dataTable-wrapper dataTable-loading no-footer sortable searchable fixed-columns">
              {/* <div className="dataTable-top">
                     <div className="dataTable-dropdown">
                        <label>
                           <select className="dataTable-selector">
                              <option value="5">5</option>
                              <option value="10" selected="">10</option>
                              <option value="15">15</option>
                              <option value="20">20</option>
                              <option value="25">25</option>
                           </select>
                           entries per page
                        </label>
                     </div>
                     <div className="dataTable-search"><input className="dataTable-input" placeholder="Search..." type="text"/></div>
                  </div> */}
              <div className="dataTable-container">
                {Object.keys(this.state.entitiesBylayouts).map((key) => (
                  this.renderTable(this.state.entitiesBylayouts[key])))}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default CatalogTable;