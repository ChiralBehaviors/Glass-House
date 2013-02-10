CnC
        .module(
                'Layout',
                function(Layout, App, Backbone, Marionette, $, _) {

                    // Layout Header View
                    // ------------------

                    Layout.Header = Backbone.Marionette.ItemView.extend({
                        template : '#template-header',

                        // UI bindings create cached attributes that
                        // point to jQuery selected objects
                        ui : {
                            input : '#new-todo'
                        },

                        events : {
                            'keypress #new-todo' : 'onInputKeypress',
                            'blur #new-todo' : 'onTodoBlur'
                        },

                        onTodoBlur : function() {
                            var todoText = this.ui.input.val().trim();
                            this.createTodo(todoText);
                        },

                        onInputKeypress : function(e) {
                            var ENTER_KEY = 13;
                            var todoText = this.ui.input.val().trim();

                            if (e.which === ENTER_KEY && todoText) {
                                this.createTodo(todoText);
                            }
                        },

                        completeAdd : function() {
                            this.ui.input.val('');
                        },

                        createTodo : function(todoText) {
                            if (todoText.trim() === "") {
                                return;
                            }

                            this.collection.create({
                                title : todoText
                            });

                            this.completeAdd();
                        }
                    });

                    // Layout Footer View
                    // ------------------

                    Layout.Footer = Backbone.Marionette.Layout
                            .extend({
                                template : '#template-footer',

                                // UI bindings create cached attributes that
                                // point to jQuery selected objects
                                ui : {
                                    todoCount : '#todo-count .count',
                                    todoCountLabel : '#todo-count .label',
                                    clearCount : '#clear-completed .count',
                                    filters : '#filters a'
                                },

                                events : {
                                    'click #clear-completed' : 'onClearClick'
                                },

                                initialize : function() {
                                    this.bindTo(App.vent, 'todoList:filter',
                                            this.updateFilterSelection, this);
                                    this.bindTo(this.collection, 'all',
                                            this.updateCount, this);
                                },

                                onRender : function() {
                                    this.updateCount();
                                },

                                updateCount : function() {
                                    var activeCount = this.collection
                                            .getActive().length, completedCount = this.collection
                                            .getCompleted().length;
                                    this.ui.todoCount.html(activeCount);
                                    this.ui.todoCountLabel
                                            .html(activeCount === 1 ? 'item'
                                                    : 'items');
                                    this.ui.clearCount
                                            .html(completedCount === 0 ? ''
                                                    : '(' + completedCount
                                                            + ')');
                                },

                                updateFilterSelection : function(filter) {
                                    this.ui.filters.removeClass('selected')
                                            .filter('[href="#' + filter + '"]')
                                            .addClass('selected');
                                },

                                onClearClick : function() {
                                    var completed = this.collection
                                            .getCompleted();
                                    completed.forEach(function destroy(todo) {
                                        todo.destroy();
                                    });
                                }
                            });

                });
