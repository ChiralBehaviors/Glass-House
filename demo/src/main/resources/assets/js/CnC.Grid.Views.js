CnC.module('Grid.Views', function(Views, App, Backbone, Marionette, $, _) {
    Views.columns = [ {
        name : "node",
        label : "Node",
        editable : false,
        cell : "string"
    }, {
        name : "HeapMemoryUsage",
        label : "Heap Memory Usage",
        cell : "integer"
    }, {
        name : "NonHeapMemoryUsage",
        label : "Non Heap Memory Usage",
        cell : "integer"
    }, {
        name : "OpenFileDescriptorCount",
        label : "Open File Descriptors",
        cell : "integer"
    }, {
        name : "SystemLoadAverage",
        label : "System Load Average",
        cell : "number"
    }, {
        name : "ProcessCpuLoad",
        label : "Process CPU Load",
        cell : "number"
    }, {
        name : "SystemCpuLoad",
        label : "System CPU Load",
        cell : "number"
    }, ];

    App.VmSummary = new Backgrid.Grid({
        columns : Views.columns,
        collection : new App.Grid.NodeCollection()
    });
});
