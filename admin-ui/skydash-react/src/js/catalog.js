import $ from 'jquery';

export var  catalog = () => {
    $.getJSON('http://localhost:8080/load-testing', function(data) {
    	$("#content-wrapper")[0].innerHTML = renderTable(data);
    });
}


function renderTable(data) {
    var headers = [];
    var headersRow = "";
    var tableContent = "<tbody> <tr class=\"odd\">";
    for (var i = 0; i < data.entities.length; i++) {
        var entity = data.entities[i];
        for (var key in entity) {
            if (entity.hasOwnProperty(key)) {
                console.log(key + " -> " + entity[key]);
                if (headers.indexOf(key) == -1) {
                    headers.push(key);
                }
                tableContent += "<td>" + entity[key] + "</td> "
            }
        }
        tableContent += "</tr>";
    }

    tableContent += "</tbody>";

    for (var header in headers) {
        headersRow += "<th>" + headers[header] + "</th>"
    }

    return "<div class=\"row\">"
                     +  "<div class=\"col-md-12 grid-margin stretch-card\">"
                          +  "<div class=\"card\">"
                            +  "<div class=\"card-body\">"
                              +  "<p class=\"card-title\">Root</p>"
                              +  "<div class=\"row\">"
                                +  "<div class=\"col-12\">"
                                  +  "<div class=\"table-responsive\">"
                                    +  "<table id=\"example\" class=\"display expandable-table\" style=\"width:100%\">"
                                      +  "<thead>"
                                        +  "<tr>"
                                          +  headersRow
                                          +  "<th></th>"
                                        +  "</tr>"
                                      +  "</thead>"
                                      + tableContent
                                      + "<tbody>"
                                  +  "</table>"
                                  +  "</div>"
                                +  "</div>"
                              +  "</div>"
                              +  "</div>"
                            +  "</div>"
                          +  "</div>"
                        +  "</div>"
}

export default catalog