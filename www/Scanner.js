var exec = require('cordova/exec');

exports.coolMethod = function(arg0, success, error) {
    exec(success, error, "Scanner", "coolMethod", [arg0]);
};

exports.scan = function(success, error) {
    exec(success, error, "Scanner", "scan", [])
};

exports.init = function(success, error) {
    console.log('About to run init')
    exec(success, error, "Scanner", "init", [])
}

exports.trigger = function(success, error) {
    exec(success, error, "Scanner", "trigger", [])
}

exports.close = function(success, error) {
    exec(success, error, "Scanner", "close", [])
}