import $ from 'jquery';

/**
* Fetch information about currently logged in user
**/
export var getUserInfo = () => {
    $.getJSON('/admin-ui/userinfo', function(data) {
      	for (var module in data.modules) {
            var myObject = eval("new " + data.modules[module].className + "(data)");
       	}
    });
}

class UserPanel {

    fillProfile(userData) {
        $("#userProfile")[0].innerHTML = '<a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown" id="profileDropdown">' +
                                       '<img src="' +
                                       userData.picture +
                                       '" alt="' + userData.name + '"/>' +
                                     '</a>' +
                                     '<div class="dropdown-menu dropdown-menu-right navbar-dropdown" aria-labelledby="profileDropdown">' +
                                       '<a class="dropdown-item">' +
                                         '<i class="ti-settings text-primary"></i>' +
                                         'Settings' +
                                       '</a>' +
                                       '<a class="dropdown-item">' +
                                         '<i class="ti-power-off text-primary"></i>' +
                                         'Logout' +
                                       '</a>'
        }

    constructor(userData) {
        this.name = "TestModule";
        console.log(this.name + " instantiated");
        this.fillProfile(userData)
    }


}