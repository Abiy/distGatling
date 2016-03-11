'use strict'

angular.module('gatling.file',[]).

    controller('fileCtrl',['$scope','$http','$routeParams',function($scope,$http,$routeParams){
        $scope.trackingId = $routeParams.id;
        $http.get('/gatling/server/upload/'+$scope.trackingId).success(function(data){
            $scope.hosts = data.hosts;
        })
    }])
