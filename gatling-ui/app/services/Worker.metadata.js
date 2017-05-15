"use strict";
var SimulationModel = (function () {
    function SimulationModel(packageName, partitionName, parameter, userName, accessKey, parallelism) {
        this.packageName = packageName;
        this.partitionName = partitionName;
        this.parameter = parameter;
        this.userName = userName;
        this.accessKey = accessKey;
        this.parallelism = parallelism;
    }
    return SimulationModel;
}());
exports.SimulationModel = SimulationModel;
//# sourceMappingURL=worker.metadata.js.map