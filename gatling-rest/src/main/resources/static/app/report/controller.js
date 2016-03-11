'use strict'

angular.module('gatling.report',[]).

    controller('reportCtrl',['$scope','$http','$routeParams',function($scope,$http,$routeParams){
        $scope.trackingId = $routeParams.id;
        $scope.report= '';


          $scope.submitReport = function(){
                   $http.post('/gatling/server/report/'+$scope.trackingId,{})
                   .success(function(data){
                       console.log(data);
                       $scope.report = data.report;
                   });
               }
    }])
