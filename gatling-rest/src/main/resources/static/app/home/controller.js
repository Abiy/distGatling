'use strict'

angular.module('gatling.home',[]).

    controller('homeCtrl',['$scope','$http',function($scope,$http){
        $http.get('/metrics').success(function(data){
            $scope.stats = data;
        })
    }])
