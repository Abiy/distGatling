(function(angular) {
  angular.module("gatling", ['ngRoute','gatling.cluster','gatling.job','gatling.tracker','gatling.home','gatling.report','gatling.file'])
  .config(['$routeProvider',function($routeProvider){
        $routeProvider
        .when("/",{templateUrl:"app/home/home.html",controller:"homeCtrl"})
        .when("/cluster",{templateUrl:"app/cluster/cluster.html",controller:"clusterCtrl"})
        .when("/job",{templateUrl:"app/job/job.html",controller:"jobCtrl"})
        .when("/tracker/:id",{templateUrl:"app/tracker/tracker.html",controller:"trackerCtrl"})
        .when("/file/:id",{templateUrl:"app/file/file.html",controller:"fileCtrl"})
        .when("/report/:id",{templateUrl:"app/report/report.html",controller:"reportCtrl"})
        .otherwise({redirectTo:"/"});
  }]);
}(angular));