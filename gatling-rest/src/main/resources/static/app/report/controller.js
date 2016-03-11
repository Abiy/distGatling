'use strict'

angular.module('gatling.report',[]).

    controller('reportCtrl',['$scope','$http','$routeParams',function($scope,$http,$routeParams){
        $scope.trackingId = $routeParams.id;
        $scope.report= '';
        $scope.inProgress= false;


          $scope.submitReport = function(){
                    $scope.inProgress = true;
                   $http.post('/gatling/server/report/'+$scope.trackingId,{})
                   .success(function(data){
                       console.log(data);
                       $scope.report = data.report;
                       $scope.inProgress = false;
                   });
               }
    }])
