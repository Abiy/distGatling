'use strict'

angular.module('gatling.job',[]).

    controller('jobCtrl',['$scope','$http',function($scope,$http){

        $scope.roleId = '';
        $scope.count = '';
        $scope.simulation = '';

        $scope.tracker = '';

        $scope.submitJob = function(){
            $http.post('/gatling/server/job',
            {
            "roleId":$scope.role,
            "count":$scope.count,
            "simulation":$scope.simulation
            })
            .success(function(data){
                console.log(data);
                $scope.tracker = data.trackingPath;
            });
        }

    }])
