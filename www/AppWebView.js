// Empty constructor
function AppWebView() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
AppWebView.prototype.coolMethod = function(arg0, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, 'AppWebView', 'coolMethod', [arg0]);
}

// Installation constructor that binds AppWebView to window
AppWebView.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.AppWebView = new AppWebView();
  return window.plugins.AppWebView;
};
cordova.addConstructor(AppWebView.install);