import { HttpHeaders } from '@angular/common/http';
import { HttpClient } from '@angular/common/http';
import { Component, SecurityContext } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import * as DOMPurify from 'dompurify';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';



@Component({
  selector: 'ngx-ecommerce',
  templateUrl: './rendered-map.component.html',
})
export class RenderedMapComponent {
  url: any = '';
  getPredictionUrl: string = "http://" + environment.backend_host + ":5000/social/api/v1.0/getpred"
  renderedMapEndpoint: string = "http://" + environment.backend_host + ":5000/render_map"
  htmlData: any = '';
  htmlString: any = '';
  isGettingMap = false
  constructor(
    private sanitizer: DomSanitizer,
    private http: HttpClient
  ) { }

  ngOnInit() {
    this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.url)
    this.getRenderedMap()
  }

  get processedDocument(): SafeHtml {
    if (this.htmlString) {
      const template = document.createElement('template');
      template.innerHTML = this.htmlString.trim()
      // const sanitized = DOMPurify.sanitize(this.htmlString, { ALLOWED_TAGS: ['meta', 'style', 'link', 'script'], RETURN_DOM: true });

      /* Add script tag */
      const script = document.createElement('script');
      script.src = 'assets/js/iframeResizer.contentWindow.js';
      template.content.appendChild(script)
      // sanitized.appendChild(script);

      /* Return result */
      // console.log("hey")
      // console.log(this.htmlString)
      // console.log(sanitized.outerHTML)
      return template.content.firstChild
    }
    return null;
  }

  public getRenderedMap() {
    let cookies: string[] = document.cookie.split(";")
    let js_raw = null
    for (let index = 0; index < cookies.length; index++) {
      if (cookies[index].indexOf("app_cookies=") >= 0) {
        let cookie = cookies[index]
        js_raw = cookie.slice(cookie.indexOf("app_cookies=") + "app_cookies=".length)
        break
      }
    }
    let cookie_object = {}
    if (js_raw) {
      cookie_object = JSON.parse(js_raw)
    }

    const headers = new HttpHeaders({
      responseType: 'text/html',
      'Access-Control-Allow-Origin': '*'
    });

    this.isGettingMap = true
    this.http.post<SafeHtml>(this.getPredictionUrl, cookie_object, { headers })
      // .pipe(catchError(err=> {err}))
      .subscribe(res => {
        this.isGettingMap = false
        this.htmlString = res;
        this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.renderedMapEndpoint)
        // this.htmlData = res
        // console.log(this.htmlData)
      })

  }

  public updateMap() {
    if (!this.isGettingMap) {
      console.log("asdf")
      this.url = this.sanitizer.bypassSecurityTrustResourceUrl('');
      this.getRenderedMap();
    }
  }

  public clearData() {
    document.cookie = "app_cookies={}; path=/"
  }
}
