/*
 * Copyright (c) 2004-2012, The Dojo Foundation All Rights Reserved.
 * Available via Academic Free License >= 2.1 OR the modified BSD license.
 * see: http://dojotoolkit.org/license for details
 */
define([
    "dojo/_base/lang", 			// lang
	"dojo/_base/declare",		// declare
	"dijit/layout/ContentPane"	// parent
], function(lang, declare, ContentPane){

// module:
//		js/LazyContentPane
// summary:
//		A demo container that sets its contents based on its title.


	return declare("dijit.layout.LazyContentPane", [ContentPane], {
		onShow: function() {
			if (this.get('content') == '') {
				this.set('content', this.onDownloadStart());
				
				var title = this.get('title');
				if (title == 'Calendar') {
					require(['dijit/Calendar'], lang.hitch(this, function(calendar) {
						this.set('content', new calendar({}));
					}));
				} else if (title == 'Color Palette') {
					require(['dijit/ColorPalette'], lang.hitch(this, function(colorPallete) {
						this.set('content', '<div></div>');
						new colorPallete({}, this.containerNode.firstChild);
					}));
				} else if (title == 'Editor') {
					require(['dijit/Editor'], lang.hitch(this, function(editor) {
						this.set('content', '<div></div>');
						new editor({
							plugins: ["bold","italic","|","cut","copy","paste","|","insertUnorderedList"]
						}, this.containerNode.firstChild);
					}));
				} else if (title == 'Chart') {
					// Chained requires for gfx are internal to the gfx module.
					require([
					    'dojox/charting/Chart2D', 
					    'dojox/charting/themes/Wetland', 
					    'dojox/charting/axis2d/Default', 
					    'dojox/charting/plot2d/Default'
					], lang.hitch(this, function(chart, wetland) {
						this.set('content', '<div></div>');
						var c = new chart(this.containerNode.firstChild);
						  c.addPlot("default", {type: "StackedAreas", tension:3})
						      .addAxis("x", {fixLower: "major", fixUpper: "major"})
							  .addAxis("y", {vertical: true, fixLower: "major", fixUpper: "major", min: 0})
						      .setTheme(wetland)
						      .addSeries("Series A", [1, 2, 0.5, 1.5, 1, 2.8, 0.4])
						      .addSeries("Series B", [2.6, 1.8, 2, 1, 1.4, 0.7, 2])
						      .addSeries("Series C", [6.3, 1.8, 3, 0.5, 4.4, 2.7, 2])
						      .render();
					}));
				}
			}
		}
	});
});
