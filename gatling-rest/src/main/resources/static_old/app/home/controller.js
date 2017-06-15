'use strict'

angular.module('gatling.home',['nvd3']).

    controller('homeCtrl',['$scope','$http',function($scope,$http){
        $scope.data = [{
                               key: "Metrics Chart",
                               values: []
                             }];
       $scope.options = {
           chart: {
               type: 'discreteBarChart',
               height: 450,
               margin : {
                   top: 20,
                   right: 20,
                   bottom: 60,
                   left: 55
               },
               x: function(d){ return d.label; },
               y: function(d){ return d.value; },
               showValues: true,
               valueFormat: function(d){
                   return d3.format(',')(d);
               },
               transitionDuration: 500,
               xAxis: {
                   axisLabel: 'X Axis'
               },
               yAxis: {
                   axisLabel: 'Y Axis',
                   axisLabelDistance: 30
               }
           }
       };

        $http.get('/metrics').success(function(data){
            $scope.stats = data;
            for (var k in data) {
            if(k.indexOf('threads.') > -1)
                $scope.data[0].values.push({label:k,value: data[k]});
            }
        })
    }])
