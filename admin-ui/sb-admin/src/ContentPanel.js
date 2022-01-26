import React from 'react';
import { useNavigate } from "react-router-dom";
import { useParams } from 'react-router';
import CatalogTable from './CatalogTable';

function ContentPanel() {
   console.log("ContentPanel init");
   let params = useParams();
   let history = useNavigate();

   return (
      <div id="layoutSidenav_content">
         <main>
            <CatalogTable contentId={params.id} history={history} />         
         </main>
         <footer className="py-4 bg-light mt-auto">
            <div className="container-fluid px-4">
               <div className="d-flex align-items-center justify-content-between small">
                  <div className="text-muted">Copyright © Your Website 2021</div>
                  <div>
                     <a href="#">Privacy Policy</a>
                     ·
                     <a href="#">Terms &amp; Conditions</a>
                  </div>
               </div>
            </div>
         </footer>
      </div>
   );
}

export default ContentPanel;