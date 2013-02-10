CnC.module('Grid', function(Grid, App, Backbone, Marionette, $, _) {
	Grid.VmSummary = Backbone.Model.extend({

        defaults : {
            node : '',
            HeapMemoryUsage : 0,
            NonHeapMemoryUsage : 0,
            OpenFileDescriptorCount : 0,
            SystemLoadAverage : 0,
            ProcessCpuLoad : 0,
            SystemCpuLoad : 0
        },
	    
	});

	Grid.NodeCollection = Backbone.Collection.extend({
		model : Grid.VmSummary
	});
});
