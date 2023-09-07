import React, { Component } from "react";
import dompurify from "dompurify";

import TableRow from "./table/TableRow";
import TableHead from "./table/TableHead";
import CatalogDataProvider from "./data/CatalogDataProvider";

const editTextFieldStyle = {
  width: '100%'
};

class CatalogTable extends Component {
  defaultPageSize = 10;
  activePages = {};
  beadcrumbs;
  /** Edit mode defines the view and element behaviour */
  isEditMode;
  /** Element being edited at the moment */
  currentlyEditableElement;
  constructor(props) {
    super(props);
    this.state = {
      contentId: props.contentId,
      history: props.history,
      parentEntity: null,
      entitiesBylayouts: {},
      scrollPositionY: 0,
      loaded: false
    };
    this.isEditMode = false;
    this.currentlyEditableElement = {
      id: NaN
    }
    this.breadcrumbs = [];
    this.rowTapped = this
      .rowTapped
      .bind(this);
    this.handleBreadCrumbClicked = this
      .handleBreadCrumbClicked
      .bind(this);
    this.confirmDeleteElement = this.confirmDeleteElement.bind(this);
    this.editElement = this.editElement.bind(this);
    this.saveElement = this.saveElement.bind(this);
    this.processElement = this.processElement.bind(this);
    this.getPaginationInfo = this.getPaginationInfo.bind(this);

    this.dataProvider = new CatalogDataProvider(this);
  }

  /**
   * Handle element rendering finished event
   */
  componentDidMount() {
    window.scrollTo(0, parseInt(sessionStorage.getItem("scrollPosition")));
  }


  componentDidMount() {
    if (!this.state.loaded) {
      this.getData(this.state.contentId);
    }
  }

  rowTapped(contentId) {
    if (!this.isEditMode) {
      this.state.loaded = false;
      this.state.history('/apicatalog/' + contentId);
      this.state.contentId = contentId;
      this.getData(this.state.contentId);
    }
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

  /**
   * 
   * @param {content id} contentId 
   * @param {parent node id} parentId
   * @param {layout name} layoutName
   * @returns real processing function
   */
  processElement(contentId, parentId, layoutName, type) {
    sessionStorage.setItem("scrollPosition", window.pageYOffset);
    if (this.isEditMode) {
      return this.saveElement(parentId, layoutName, type);
    }
    return this.editElement(contentId);
  }

  /**
   * Edit content element
   * @param {content node identifier} contentId 
   */
  editElement(contentId) {
    this.isEditMode = true;
    this.currentlyEditableElement["id"]= contentId;
    this.setState(this.state);
  }

  /**
   * Save element being edited at the moment
   * @param {content identifier} contentId 
   */
  saveElement(parentId, layoutName, type) {
    this.saveData(this.currentlyEditableElement, parentId, layoutName, type);
    this.isEditMode = false;
    this.currentlyEditableElement = {id: NaN};
  }
  
  getEditOrSaveIcon() {
    return this.isEditMode ? "fa-save" : "fa-edit";
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

  async getData(id) {
    if (!this.state.loaded) {
      if (id == null) {
        id = 0;
      }
      await this.dataProvider.getContent(id)
      .then(res => res.json())
      .then(
        (result) => this.successGetCallback(result),
        (error) => this.errorCallback(error)
      );
    }
  }

  async saveData(content, parentId, layoutName, type) {
      content["type"] = layoutName;
      console.log("Update entity request");
      // todo link from configuration
      await this.dataProvider.saveData(content, parentId, layoutName)
        .then(res => res.json())
        .then(
          (result) => {
            for (var entity in this.state.entitiesBylayouts[result.layout.name].entities) {
                if (this.state.entitiesBylayouts[result.layout.name].entities[entity].id === result.entity.id) {
                  this.state.entitiesBylayouts[result.layout.name].entities[entity] = result.entity;
                }
            }
            //this.setState(this.state);
          },
          (error) => {
            console.log("Error during request: " + error);
          }
        )
    
  }

  successGetCallback(result) {
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
    if (result.parents) {
      this.breadcrumbs = result.parents.reverse();
    }

    this.setState({
      entitiesBylayouts: entitiesBylayouts,
      parentEntity: parent,
      parentLayout: parentLayout,
      loaded: true
    });
  }

  errorCallback(error) {
    console.log("Error during request: " + error);
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
  renderTableItem(item, key, attribute, id) {
    if (this.isEditMode && this.currentlyEditableElement.id == id) {
      console.log("entering edit mode");
      return this.renderTableItemInEditMode(item, key, attribute);
    } else {
      return this.renderTableItemInReadMode(item, attribute);
    }
  }

  renderBreadCrumbs() {
    return (<ol className="breadcrumb mb-4">
      <li className="breadcrumb-item">
        {/*  todo root element for current user */}
        <a href="/apicatalog/0">Root</a></li>
      {Object.values(this.breadcrumbs).map(breadcrumb => (<li key={breadcrumb.id} onClick={() => this.handleBreadCrumbClicked(breadcrumb.id)} className="breadcrumb-item active">{breadcrumb.stringValue}</li>))}

    </ol>);
  }

  getSubList(tableElements) {
    let result = [];
    for (let index = this.getCurrentPage(tableElements) * this.defaultPageSize; 
      index < (this.activePages[tableElements.layout.name].currentPage  + 1)* this.defaultPageSize;
      index++) {
        if (tableElements.entities.length > index) {
          result.push(tableElements.entities[index]);
        }
      }
    return result;
  }

  getCurrentPage(tableElements) {
    return this.activePages[tableElements.layout.name] 
      ? this.activePages[tableElements.layout.name].currentPage
      : 0;
  }

  setCurrentPage(layoutName, index) {
    console.log("[DEBUG] Setting page index = " + index + " for layout " + layoutName);
    if (!this.isEditMode && this.activePages[layoutName].currentPage != parseInt(index)) {
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
      <li key={parseInt(selectElement) + 1} className={className}>
        <a id={selectElement} onClick={() => this.setCurrentPage(tableElements.layout.name, selectElement)}>{parseInt(selectElement) + 1}</a>
      </li>
      );
    }
    )
    return (<ul className="dataTable-pagination-list">{elements}</ul>);
    
  }

  renderTable(tableElements, contentId) {
    if (tableElements.layout.attributes == null || tableElements.entities.length == 0) {
      return (<div></div>);
    }
    return (
      <div>
        <p>{tableElements.layout.name}</p>
        <table id={tableElements.layout.name} className="dataTable-table">
          <TableHead layout={tableElements.layout}/>
          <tbody>
            {this.getSubList(tableElements)
              .map((item) => (
              <TableRow parentId={contentId} item={item} attributes={tableElements.layout.attributes} handleRowTapped={this.rowTapped} editMode={false}/>
            ))}
          </tbody>
        </table>
        <div className="dataTable-bottom">
          <div className="dataTable-info">{this.getPaginationInfo(tableElements.layout.name)}</div>
          <nav className="dataTable-pagination">
              {this.renderTablePagination(tableElements)}
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
                  this.renderTable(this.state.entitiesBylayouts[key], this.state.contentId)))}
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

export default CatalogTable;