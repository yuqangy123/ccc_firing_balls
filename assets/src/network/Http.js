
//字符串长度 用于签名算法的 签名算法中文服务器中文是1个长度 js之前是两个 导致不一致
var getStrLeng = function (str) {
    var realLength = 0;
    var len = str.length;
    var charCode = -1;
    for (var i = 0; i < len; i++) {
        charCode = str.charCodeAt(i);
        if (charCode >= 0 && charCode <= 128) {
            realLength += 1;
        } else {
            if (charCode < 2048) {
                realLength += 2;
            } else {
                if (charCode < 65536) {
                    realLength += 3;
                } else {
                    //这里偷懒了，其他全部算6字节了。计算中英文，这个计算方法应该是够用了。
                    realLength += 6;
                }

            }
        }
    }
    return realLength;
}

//readyState
// 0：初始化，XMLHttpRequest对象还没有完成初始化
// 1：载入，XMLHttpRequest对象开始发送请求
// 2：载入完成，XMLHttpRequest对象的请求发送完成
// 3：解析，XMLHttpRequest对象开始读取服务器的响应
// 4：完成，XMLHttpRequest对象读取服务器响应结束
var httpGet = function (url, callback, timeout, bNeedAccess) {
    var xhr = new XMLHttpRequest();
    //Access-Control-Allow-Origin报错解决方案 
    if (bNeedAccess && !cc.sys.isNative && cc.sys.platform != cc.sys.WECHAT_GAME) {
        url = "https://www.kylinlusoft.com/index.php?url=" + url;
    }
    xhr.open("GET", url, true);
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                cc.warn("httpGet[" + url + "] error->status:" + xhr.status + ' response:' + xhr.responseText);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpGet timeout url:" + url);
    }

    xhr.send();
}

var getHttpUrlPostParam = function (param) {
    var param = param || {};
    var userID = TEST_PLAYBACK_USER_ID || app.userMgr.getUserID();
    userID && (param.user_id = userID);

    param.secret_token = app.userMgr.getUserSecretToken()

    var signl = [];
    for (const key in param) {
        signl.push(param[key].toString());
    }
    signl.sort(function (a, b) {
        var aLen = getStrLeng(a);
        var bLen = getStrLeng(b);
        if (aLen == 0 || bLen == 0 || aLen == bLen) return -1;
        var cnt = Math.min(aLen, bLen) - 1, inc = 0;
        while (cnt >= inc) {
            var aCode = a.charCodeAt(inc);
            var bCode = b.charCodeAt(inc);
            if (aCode != bCode) return aCode < bCode ? -1 : 1;
            inc += 1;
        }
        return cnt == aLen ? -1 : 1;
    });

    param.sign = "";
    var signlLen = signl.length;
    for (let idx = 0; idx < signlLen; idx++) {
        param.sign += signl[idx];
    }
    param.sign = require("md5").hex_md5(param.sign);
    delete param['secret_token'];

    var res = "";
    for (const key in param) {
        res = res + key + "=" + param[key] + "&";
    }
    res = res.substring(0, (res.length - 1));
    return res;
}

var getHttpUrlPostParamExx = function (param) {
    cc.log('getHttpUrlPostParamExx start:', param)
    var param = param || {};
    var tokenInfo = app.userMgr.getTokenInfo();

    param.token_id = tokenInfo.token_id;
    if (param.user_id) delete param.user_id;
    param.user_id = app.userMgr.getUserID();
    var signl = [];
    for (const key in param) {
        signl.push(key + "");
    }
    signl.sort(function (a, b) {
        var aLen = getStrLeng(a);
        var bLen = getStrLeng(b);
        if (aLen == 0 || bLen == 0 || (a == b)) return 0;
        var cnt = Math.min(aLen, bLen) - 1, inc = 0;
        while (cnt >= inc) {
            var aCode = a.charCodeAt(inc);
            var bCode = b.charCodeAt(inc);
            if (aCode != bCode) {
                return ((aCode < bCode) ? -1 : 1);
            }
            inc += 1;
        }
        return (cnt == alen) ? -1 : 1;
    });
    param.key = tokenInfo.token_secret_key;
    signl.push("key");

    var res = "";
    var signlLen = signl.length;
    for (let idx = 0; idx < signlLen; idx++) {
        res = res + signl[idx] + "=" + param[signl[idx]] + "&";
    }
    res = res.substring(0, (res.length - 1));
    cc.log('getHttpUrlPostParamExx middle:', res)
    param.sign = require("md5").hex_md5(res);
    delete param.key;
    for (const key in param) {
        param[key] = encodeURIComponent(param[key]);
    }

    res = "";
    for (const key in param) {
        res = res + key + "=" + param[key] + "&";
    }
    res = res.substring(0, (res.length - 1));
    cc.log('getHttpUrlPostParamExx end:', res)
    return res;
}

var httpPost = function (url, params, callback, timeout) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                cc.log("httpPost[" + url + "] status:", xhr.status);
                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                cc.warn("httpPost[" + url + "] error->status:" + xhr.status);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpPost timeout url:" + url);
    }

    if (params) {
        xhr.send(params);
    } else {
        xhr.send();
    }
}

var httpPostEx = function (url, params, callback, timeout) {
    var xhr = new XMLHttpRequest();

    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                cc.log("httpPost[" + url + "] response:", response);
                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                cc.warn("httpPost[" + url + "] error->status:" + xhr.status);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpPostEx timeout url:" + url);
    }

    if (params) {
        var m_params = getHttpUrlPostParam(params);
        xhr.send(m_params);
    } else {
        xhr.send();
    }
}

/**带上token请求 */
var httpPostToken = function (url, params, callback, timeout) {
    var token = app.userMgr.getToken();
    if (null == token) {
        app.showFloatTips('参数错误，请稍后尝试');
        return
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.setRequestHeader("x-xq5-jwt", token);
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                cc.log("httpPost[" + url + "] status:", xhr.status);
                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                cc.warn("httpPost[" + url + "] error->status:" + xhr.status);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpPostToken timeout url:" + url);
    }

    if (params) {
        var m_params = getHttpUrlPostParam(params);
        xhr.send(m_params);
    } else {
        xhr.send();
    }
}

var httpPostClub = function (url, params, callback, timeout) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                cc.log("httpPost[" + url + "] response:", response);
                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                cc.warn("httpPost[" + url + "] error->status:" + xhr.status);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpPostClub timeout url:" + url);
    }

    if (params) {
        var m_params = getHttpUrlPostParamExx(params);
        xhr.send(m_params);
    } else {
        xhr.send();
    }
}

var httpPostJson = function (url, params, callback, timeout, is_author, error_callback) {
    var xhr = new XMLHttpRequest();
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    if (true == is_author) {
        // hash = MD5("date=今天日期&secret=客户端密钥")
        // 示例:
        // hash = MD5("date=2019-05-04&secret=1UD9DI") = "D3B37D5B6EDFC9FB93A92C6CEC81708E"
        var now = new Date();
        var author = require("md5").hex_md5(string.format('date=%04d-%02d-%02d&secret=6B21U9SQ', now.getFullYear(), now.getMonth() + 1, now.getDate()));
        xhr.setRequestHeader("Authorization", author.toUpperCase());
    }
    xhr.onreadystatechange = function () {
        if (xhr.readyState == 4) {
            if (xhr.status >= 200 && xhr.status < 400) {
                var response = xhr.responseText;
                cc.log("httpPostJson[" + url + "] status:", xhr.status);

                if (callback) {
                    callback(xhr.status, response);
                }
            } else {
                if (error_callback) {
                    error_callback(xhr.status, xhr.responseText);
                }
                cc.warn("httpPostJson[" + url + "] error->status:" + xhr.status + ' error->respone:' + xhr.responseText);
            }
        }
    };

    if (timeout) {
        xhr.timeout = timeout;
    }
    //超时回调记录
    xhr.ontimeout = function () {
        cc.log("httpPostJson timeout url:" + url);
    }

    if (params) {
        // cc.log("=================httpPost[" + url + "] params:", params);
        xhr.send(params);
    } else {
        xhr.send();
    }
}

module.exports = {
    httpGet,
    httpPost,
    httpPostEx,
    httpPostJson,
    httpPostClub,
    httpPostToken,
}