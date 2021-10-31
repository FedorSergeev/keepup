import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

import 'datatables.net-bs4'
// import './vendors/js/vendor.bundle.base.js'

// import './vendors/datatables.net/jquery.dataTables.js'
// import './vendors/datatables.net-bs4/dataTables.bootstrap4.js'
// import './js/dataTables.select.min.js'
import './js/off-canvas.js'
// import './js/hoverable-collapse.js'
import './js/template.js'
// import './js/settings.js'
// import './js/todolist.js'
import './js/dashboard.js'
import './js/keepup.js'
import './js/catalog.js'
// import './js/Chart.roundedBarCharts.js'

ReactDOM.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
  document.getElementById('root')
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
