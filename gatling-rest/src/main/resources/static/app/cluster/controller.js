'use strict'

angular.module('gatling.cluster',[]).

    controller('clusterCtrl',['$scope','$http',function($scope,$http){
        $http.get('/gatling/server/info').success(function(data){
            $scope.workers = data;
        })
    }])
