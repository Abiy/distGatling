if ('undefined' !== typeof module) {

    module.exports = function initDemo(dashboardData){

        var dataHost = {
            labels: dashboardData.host.keys,
            series: [
                dashboardData.host.values
            ]
        };


        var optionsHost = {
            seriesBarDistance: 10,
            axisX: {
                showGrid: false
            },
            height: "245px",
            axisY: {
                onlyInteger: true,
            }
        };

        var responsiveHost = [
            ['screen and (max-width: 640px)', {
                seriesBarDistance: 5,
                axisX: {
                    labelInterpolationFnc: function (value) {
                        return value[0];
                    }
                }
            }]
        ];

        Chartist.Bar('#chartHost', dataHost, optionsHost, responsiveHost);



        var dataPartition = {
            labels: dashboardData.partition.keys,
            series: [
                dashboardData.partition.values
            ]
        };

        var optionsPartition = {
            seriesBarDistance: 10,
            axisX: {
                showGrid: false
            },
            height: "245px"
        };

        var responsiveOptionsPartition = [
            ['screen and (max-width: 640px)', {
                seriesBarDistance: 5,
                axisX: {
                    labelInterpolationFnc: function (value) {
                        return value[0];
                    }
                }
            }]
        ];

        Chartist.Bar('#chartPartition', dataPartition, optionsPartition, responsiveOptionsPartition);


        var dataPartitionStatus = {
            series: [
                dashboardData.partitionStatus.values
            ],
            labels:[
                dashboardData.partitionStatus.keys
                ]
        };

        var optionsPartitionStatus = {
            donut: true,
            donutWidth: 40,
            startAngle: 0,
            total: 100,
            showLabel: true,
            axisX: {
                showGrid: true
            }
        };

        Chartist.Pie('#partitionStatus', dataPartitionStatus, optionsPartitionStatus);

        Chartist.Pie('#partitionStatus', {
            labels: dashboardData.partitionStatus.keys,
            series: dashboardData.partitionStatus.values
        });

    }

}
