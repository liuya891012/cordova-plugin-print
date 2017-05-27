var exec = require('cordova/exec');


var PrintPlugin = function () {}
// arg1：成功回调; arg2：失败回调; arg3：将要调用类配置的标识; arg4：调用的原生方法名; arg5：参数，json格式
  PrintPlugin.prototype.openBluetooth = function (success, error) {
    exec(success, error, "PrintPlugin", "openBluetooth", []);
  };
   PrintPlugin.prototype.getPairedEqu = function (success, error) {
    exec(success, error, "PrintPlugin", "getPairedEqu", []);
  };
  PrintPlugin.prototype.searchEqu = function (success, error) {
    exec(success, error, "PrintPlugin", "searchEqu", []);
  };
  PrintPlugin.prototype.paireEqu = function (success, error,arg0) {
    exec(success, error, "PrintPlugin", "paireEqu", [arg0]);
  };
  PrintPlugin.prototype.disconnectEqu = function (success, error) {
	exec(success, error, "PrintPlugin", "disconnectEqu", []);
  };
  PrintPlugin.prototype.getStatus = function (success, error) {
	exec(success, error, "PrintPlugin", "getStatus", []);
  };
  PrintPlugin.prototype.printMaterial = function (success, error,arg0,arg1,arg2,arg3,arg4,arg5) {
  exec(success, error, "PrintPlugin", "printMaterial", [arg0,arg1,arg2,arg3,arg4,arg5]);
  };
  PrintPlugin.prototype.printCell = function (success, error,arg0,arg1) {
  exec(success, error, "PrintPlugin", "printCell", [arg0,arg1]);
  };
  PrintPlugin.prototype.printPic= function (success, error,arg0) {
  exec(success, error, "PrintPlugin", "printPic", [arg0]);
  };
  PrintPlugin.prototype.closeBluetooth = function (success, error) {
  exec(success, error, "PrintPlugin", "closeBluetooth", []);
  };
var printPlugin=new PrintPlugin();
module.exports = printPlugin;
