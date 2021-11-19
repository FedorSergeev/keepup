import React from 'react';
import { Route, Routes } from 'react-router';

// import {
//   Routes,
//   Route
// } from "react-router-dom";
import LeftPanel from './LeftPanel';
import ContentPanel from './ContentPanel';

function LayoutSideNav() {
  
      return (
        <div id="layoutSidenav">
          
            <LeftPanel/>
            <Routes>
              <Route exact path="/apicatalog/:id" element={<ContentPanel/>}/>
            </Routes>
            
        </div>
      );
    }
  

  export default LayoutSideNav;