import { Injectable } from '@angular/core';
import { HttpEvent, HttpErrorResponse, HttpInterceptor, HttpHandler, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import {catchError} from "rxjs/operators";
import {of} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ErrorInterceptor implements HttpInterceptor {
    constructor(private router: Router) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        return next.handle(request).pipe(
            catchError(
                (error:any, caught: Observable<HttpEvent<any>>) => {

                    if (error.status == 401 || error.status == 0 || error.status == 404) {
                        this.router.navigate(['/login'], { queryParams: { cause: error.message }});
                        return of(error) ;
                    }

                    // if (error.status === 401) {
                    //     localStorage.setItem('currentUser', null);
                    //     this.router.navigate(['/login']);
                    //     return of(error)
                    // }
                    throw error;
                }
            )
        )
    }
}