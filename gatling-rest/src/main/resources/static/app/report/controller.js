'use strict'

angular.module('gatling.report',[]).

    controller('reportCtrl',['$scope','$http','$routeParams',function($scope,$http,$routeParams){
        $scope.trackingId = $routeParams.id;
        $scope.report= '';
        $scope.inProgress= false;
        $scope.showError= false;


          $scope.submitReport = function(){
                    $scope.inProgress = true;
                   $http.post('/gatling/server/report/'+$scope.trackingId,{})
                   .success(function(data){
                        console.log(data);
                        $scope.report = data.report;
                        $scope.inProgress = false;
                   })
                   .error(function(data){
                       console.log('Error: This could be because the job is cancelled or the reporting the job timing out.');
                       $scope.showError = true;
                       $scope.inProgress = false;
                      });
               }
    }])
