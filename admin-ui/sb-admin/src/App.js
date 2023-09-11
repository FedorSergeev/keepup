import React, { Component } from 'react';
import { createBrowserHistory } from "history";

import './css/styles.css';
import './css/admin.css';
import './css/all.css';
import Navbar from './Navbar';
import LayoutSideNav from './LayoutSideNav';
import { BrowserRouter as Router } from "react-router-dom";

class App extends Component {
  
  state = {};
  render() {
    const handleScroll = event => {
      sessionStorage.setItem("scrollPosition", window.scrollY);
    };

    return (

      <Router history={createBrowserHistory()}>
        <Navbar />
        <LayoutSideNav/>
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js" crossOrigin="anonymous"></script>
        <script src="js/scripts.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/simple-datatables@latest" crossOrigin="anonymous"></script>
        <script src="js/datatables-simple-demo.js"></script>
      </Router>
    );
  }
}

export default App;
