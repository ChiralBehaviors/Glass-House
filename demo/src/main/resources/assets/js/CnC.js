var CnC = new Backbone.Marionette.Application();

CnC.addRegions({
	header : '#header',
	main : '#main',
	footer : '#footer'
});

CnC.on('initialize:after', function() {
	Backbone.history.start();
});
