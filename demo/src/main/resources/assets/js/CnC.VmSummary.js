CnC.module('VmSummary', function(TodoList, App, Backbone, Marionette, $, _) {

	// CnC Router
	// ---------------
	//
	// Handle routes to show the active vs complete todo items

	CnC.Router = Marionette.AppRouter.extend({
		appRoutes : {
			'*filter' : 'filterItems'
		}
	});

	// CnC Controller (Mediator)
	// ------------------------------
	//
	// Control the workflow and logic that exists at the application
	// level, above the implementation detail of views and models

	CnC.Controller = function() {
	};

	_.extend(CnC.Controller.prototype, {

		// Start the app by showing the appropriate views
		// and fetching the list of todo items, if there are any
		start : function() { 
		    this.showGrid();
		    /*
			this.showHeader(this.todoList);
			this.showFooter(this.todoList);
			this.showTodoList(this.todoList);
			this.showGrid(this.userList);
			this.showGrid2(this.userList);

			App.bindTo(this.todoList, 'reset add remove', this.toggleFooter,
					this);
			this.todoList.fetch();
			*/
		},

		showGrid : function() {
			App.main.show(App.VmSummary);
		},

		showHeader : function(todoList) {
			var header = new App.Layout.Header({
				collection : todoList
			});
			App.header.show(header);
		},

		showFooter : function(todoList) {
			var footer = new App.Layout.Footer({
				collection : todoList
			});
			App.footer.show(footer);
		},

		showTodoList : function(todoList) {
			App.main.show(new TodoList.Views.ListView({
				collection : todoList
			}));
		},

		toggleFooter : function() {
			App.footer.$el.toggle(this.todoList.length);
		},

		// Set the filter to show complete or all items
		filterItems : function(filter) {
			App.vent.trigger('todoList:filter', filter.trim() || '');
		}
	});

	// CnC Initializer
	// --------------------
	//
	// Get the CnC up and running by initializing the mediator
	// when the the application is started, pulling in all of the
	// existing nodes and displaying the VM Summary.

	CnC.addInitializer(function() {
		var controller = new CnC.Controller();
		new CnC.Router({
			controller : controller
		});

		controller.start();
	});

});
