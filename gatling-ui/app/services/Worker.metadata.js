"use strict";
var SimulationModel = (function () {
    function SimulationModel(packageName, partitionName, tag, userName, accessKey, parallelism) {
        this.packageName = packageName;
        this.partitionName = partitionName;
        this.tag = tag;
        this.userName = userName;
        this.accessKey = accessKey;
        this.parallelism = parallelism;
    }
    return SimulationModel;
}());
exports.SimulationModel = SimulationModel;
//# sourceMappingURL=worker.metadata.js.map