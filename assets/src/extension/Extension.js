
window.string = {};
string.format = function () {
    //  discuss at: http://locutus.io/php/sprintf/
    // original by: Ash Searle (http://hexmen.com/blog/)
    // improved by: Michael White (http://getsprink.com)
    // improved by: Jack
    // improved by: Kevin van Zonneveld (http://kvz.io)
    // improved by: Kevin van Zonneveld (http://kvz.io)
    // improved by: Kevin van Zonneveld (http://kvz.io)
    // improved by: Dj
    // improved by: Allidylls
    //    input by: Paulo Freitas
    //    input by: Brett Zamir (http://brett-zamir.me)
    // improved by: Rafał Kukawski (http://kukawski.pl)
    //   example 1: sprintf("%01.2f", 123.1)
    //   returns 1: '123.10'
    //   example 2: sprintf("[%10s]", 'monkey')
    //   returns 2: '[    monkey]'
    //   example 3: sprintf("[%'#10s]", 'monkey')
    //   returns 3: '[####monkey]'
    //   example 4: sprintf("%d", 123456789012345)
    //   returns 4: '123456789012345'
    //   example 5: sprintf('%-03s', 'E')
    //   returns 5: 'E00'
    //   example 6: sprintf('%+010d', 9)
    //   returns 6: '+000000009'
    //   example 7: sprintf('%+0\'@10d', 9)
    //   returns 7: '@@@@@@@@+9'
    //   example 8: sprintf('%.f', 3.14)
    //   returns 8: '3.140000'
    //   example 9: sprintf('%% %2$d', 1, 2)
    //   returns 9: '% 2'

    var regex = /%%|%(?:(\d+)\$)?((?:[-+#0 ]|'[\s\S])*)(\d+)?(?:\.(\d*))?([\s\S])/g
    var args = arguments
    var i = 0
    var format = args[i++]

    var _pad = function (str, len, chr, leftJustify) {
        if (!chr) {
            chr = ' '
        }
        var padding = (str.length >= len) ? '' : new Array(1 + len - str.length >>> 0).join(chr)
        return leftJustify ? str + padding : padding + str
    }

    var justify = function (value, prefix, leftJustify, minWidth, padChar) {
        var diff = minWidth - value.length
        if (diff > 0) {
            // when padding with zeros
            // on the left side
            // keep sign (+ or -) in front
            if (!leftJustify && padChar === '0') {
                value = [
                    value.slice(0, prefix.length),
                    _pad('', diff, '0', true),
                    value.slice(prefix.length)
                ].join('')
            } else {
                value = _pad(value, minWidth, padChar, leftJustify)
            }
        }
        return value
    }

    var _formatBaseX = function (value, base, leftJustify, minWidth, precision, padChar) {
        // Note: casts negative numbers to positive ones
        var number = value >>> 0
        value = _pad(number.toString(base), precision || 0, '0', false)
        return justify(value, '', leftJustify, minWidth, padChar)
    }

    // _formatString()
    var _formatString = function (value, leftJustify, minWidth, precision, customPadChar) {
        if (precision !== null && precision !== undefined) {
            value = value.slice(0, precision)
        }
        return justify(value, '', leftJustify, minWidth, customPadChar)
    }

    // doFormat()
    var doFormat = function (substring, argIndex, modifiers, minWidth, precision, specifier) {
        var number, prefix, method, textTransform, value

        if (substring === '%%') {
            return '%'
        }

        // parse modifiers
        var padChar = ' ' // pad with spaces by default
        var leftJustify = false
        var positiveNumberPrefix = ''
        var j, l

        for (j = 0, l = modifiers.length; j < l; j++) {
            switch (modifiers.charAt(j)) {
                case ' ':
                case '0':
                    padChar = modifiers.charAt(j)
                    break
                case '+':
                    positiveNumberPrefix = '+'
                    break
                case '-':
                    leftJustify = true
                    break
                case "'":
                    if (j + 1 < l) {
                        padChar = modifiers.charAt(j + 1)
                        j++
                    }
                    break
            }
        }

        if (!minWidth) {
            minWidth = 0
        } else {
            minWidth = +minWidth
        }

        if (!isFinite(minWidth)) {
            throw new Error('Width must be finite')
        }

        if (!precision) {
            precision = (specifier === 'd') ? 0 : 'fFeE'.indexOf(specifier) > -1 ? 6 : undefined
        } else {
            precision = +precision
        }

        if (argIndex && +argIndex === 0) {
            throw new Error('Argument number must be greater than zero')
        }

        if (argIndex && +argIndex >= args.length) {
            throw new Error('Too few arguments')
        }

        value = argIndex ? args[+argIndex] : args[i++]

        switch (specifier) {
            case '%':
                return '%'
            case 's':
                return _formatString(value + '', leftJustify, minWidth, precision, padChar)
            case 'c':
                return _formatString(String.fromCharCode(+value), leftJustify, minWidth, precision, padChar)
            case 'b':
                return _formatBaseX(value, 2, leftJustify, minWidth, precision, padChar)
            case 'o':
                return _formatBaseX(value, 8, leftJustify, minWidth, precision, padChar)
            case 'x':
                return _formatBaseX(value, 16, leftJustify, minWidth, precision, padChar)
            case 'X':
                return _formatBaseX(value, 16, leftJustify, minWidth, precision, padChar)
                    .toUpperCase()
            case 'u':
                return _formatBaseX(value, 10, leftJustify, minWidth, precision, padChar)
            case 'i':
            case 'd':
                number = +value || 0
                // Plain Math.round doesn't just truncate
                number = Math.round(number - number % 1)
                prefix = number < 0 ? '-' : positiveNumberPrefix
                value = prefix + _pad(String(Math.abs(number)), precision, '0', false)

                if (leftJustify && padChar === '0') {
                    // can't right-pad 0s on integers
                    padChar = ' '
                }
                return justify(value, prefix, leftJustify, minWidth, padChar)
            case 'e':
            case 'E':
            case 'f': // @todo: Should handle locales (as per setlocale)
            case 'F':
            case 'g':
            case 'G':
                number = +value
                prefix = number < 0 ? '-' : positiveNumberPrefix
                method = ['toExponential', 'toFixed', 'toPrecision']['efg'.indexOf(specifier.toLowerCase())]
                textTransform = ['toString', 'toUpperCase']['eEfFgG'.indexOf(specifier) % 2]
                value = prefix + Math.abs(number)[method](precision)
                return justify(value, prefix, leftJustify, minWidth, padChar)[textTransform]()
            default:
                // unknown specifier, consume that char and return empty
                return ''
        }
    }

    try {
        return format.replace(regex, doFormat)
    } catch (err) {
        return false
    }
};

string.Utf8ArrayToStr = function (array) {
    var out, i, len, c;
    var char2, char3;

    out = "";
    len = array.length;
    i = 0;
    while (i < len) {
        c = array[i++];
        switch (c >> 4) {
            case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                // 0xxxxxxx
                out += String.fromCharCode(c);
                break;
            case 12: case 13:
                // 110x xxxx   10xx xxxx
                char2 = array[i++];
                out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
                break;
            case 14:
                // 1110 xxxx  10xx xxxx  10xx xxxx
                char2 = array[i++];
                char3 = array[i++];
                out += String.fromCharCode(((c & 0x0F) << 12) |
                    ((char2 & 0x3F) << 6) |
                    ((char3 & 0x3F) << 0));
                break;
        }
    }

    return out;
};

//随机整数 [min,max)
Math.randomInt = function (min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

//适用于单独数据的深度拷贝
window.DeepCopy = function (obj) {
    return JSON.parse(JSON.stringify(obj));
}

/**
 * 替换给定的次数
 */
String.prototype.replaceWithCount = function (pattern, repl, n) {
    var result = this;
    var replaceCout = 0;
    while (result.indexOf(pattern) != -1 && (null == n || replaceCout < n)) {
        result = result.replace(pattern, repl);
        replaceCout++;
        if (result == '') {
            break;
        }
    }
    return { str: result, count: replaceCout };
}

/**
 * 子串出现的次数
 */
String.prototype.charCount = function (pattern) {
    var count = 0;
    var index = 0;
    while (true) {
        index = this.indexOf(pattern, index)
        if (index == -1) {
            break;
        }
        count++;
        index = index + pattern.length;
    }
    return count;
}

cc.Node.prototype.removeAllChildren = function (cleanup) {
    //为何重写
    // 从 < 2.2.0 版本升级
    // 从 2.2.0 开始，我们强化了内存管理机制，现在要求用户通过代码动态创建且独立于场景节点树的 cc.Node 必须通过 destroy() 释放，否则引擎无法知道何时回收这类节点的内存，会导致内存泄露。

    // 如原先手动从场景中移除的节点，在不需要用到的时候也需要统一 destroy() ：
    // // 假设 testNode 是场景中的某个节点，若之前被手动移出场景了，如
    // testNode.parent = null;
    // // 或者
    // testNode.removeFromParent(true);
    // // 或者
    // parentNode.removeChild(testNode);
    // // 若往后 testNode 还会再次用到，则无需手动 destroy 该节点
    // // 否则应该手动调用
    // testNode.destroy();
    // 如原先通过 cc.NodePool 管理节点，则不受影响。
    //重写这个方法新增 node.destroy()调用  主要是之前动态创建的节点都没有考虑到释放问题 量比较大 这样做统一修改
    // cc.warn("removeAllChildren 方法已经被重写，使用请慎重@kylin");
    // not using detachChild improves speed here
    var children = this._children;
    if (cleanup === undefined)
        cleanup = true;
    for (var i = children.length - 1; i >= 0; i--) {
        var node = children[i];
        if (node) {
            // If you don't do cleanup, the node's actions will not get removed and the
            if (cleanup)
                node.cleanup();

            node.parent = null;
            node.destroy();
        }
    }
    this._children.length = 0;
}


// 对Date的扩展，将 Date 转化为指定格式的String
// 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
// 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
// 例子： 
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
// (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份 
        "d+": this.getDate(), //日 
        "H+": this.getHours(), //小时 
        "m+": this.getMinutes(), //分 
        "s+": this.getSeconds(), //秒 
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
        "S": this.getMilliseconds() //毫秒 
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
    if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}