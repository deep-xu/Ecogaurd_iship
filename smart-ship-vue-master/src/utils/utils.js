export function extendjs() {
  Date.prototype.format = function (pattern) {
    /*初始化返回值字符串*/
    var returnValue = pattern;
    /*正则式pattern类型对象定义*/
    var format = {
      "Y+": this.getFullYear(),
      "y+": this.getFullYear(),
      "M+": this.getMonth() + 1,
      "D+": this.getDate(),
      "d+": this.getDate(),
      "H+": this.getHours(),
      "m+": this.getMinutes(),
      "s+": this.getSeconds(),
      "S": this.getMilliseconds(),
      "h+": (this.getHours() % 12),
      "a": (this.getHours() / 12) <= 1 ? "AM" : "PM"
    };
    /*遍历正则式pattern类型对象构建returnValue对象*/
    for (var key in format) {
      var regExp = new RegExp("(" + key + ")");
      if (regExp.test(returnValue)) {
        var zero = "";
        for (var i = 0; i < RegExp.$1.length; i++) { zero += "0"; }
        var replacement = RegExp.$1.length == 1 ? format[key] : (zero + format[key]).substring((("" + format[key]).length));
        returnValue = returnValue.replace(RegExp.$1, replacement);
      }
    }
    return returnValue;
  }

  // String.prototype.replaceAll = function (s1, s2) {
  //   return this.replace(new RegExp(s1, "gm"), s2);
  // }

  //数组迭代函数
  Array.prototype.each = function (fn) {
    fn = fn || Function.K;
    var a = [];
    var args = Array.prototype.slice.call(arguments, 1);
    for (var i = 0; i < this.length; i++) {
      var res = fn.apply(this, [this[i], i].concat(args));
      if (res != null) a.push(res);
    }
    return a;
  };
  //数组是否包含指定元素
  Array.prototype.contains = function (suArr) {
    for (var i = 0; i < this.length; i++) {
      if (this[i] == suArr) {
        return true;
      }
    }
    return false;
  }

  //不重复元素构成的数组
  Array.prototype.uniquelize = function () {
    var ra = new Array();
    for (var i = 0; i < this.length; i++) {
      if (!ra.contains(this[i])) {
        ra.push(this[i]);
      }
    }
    return ra;
  };

  //两个数组的交集
  Array.intersect = function (a, b) {
    return a.uniquelize().each(function (o) { return b.contains(o) ? o : null });
  };
  //两个数组的差集
  Array.minus = function (a, b) {
    return a.uniquelize().each(function (o) { return b.contains(o) ? null : o });
  };
  //两个数组的补集
  Array.complement = function (a, b) {
    return Array.minus(Array.union(a, b), Array.intersect(a, b));
  };
  //两个数组并集
  Array.union = function (a, b) {
    return a.concat(b).uniquelize();
  };
}
export function resizeFontsize() {
  (function (doc, win) {
    var docEl = doc.documentElement,
      resizeEvt = "orientationchange" in window ? "orientationchange" : "resize",
      recalc = function () {
        var clientWidth = docEl.clientWidth;
        if (!clientWidth) {
          return
        }
        // 1920px时 1rem == 1px
        if (clientWidth > 1200) {
          docEl.style.fontSize = clientWidth / 1920 + "px"
        } else {
          docEl.style.fontSize = 1200 / 1920 + "px"
        }
      };

    if (!doc.addEventListener) {
      return
    }
    win.addEventListener(resizeEvt, () => {
      recalc();
    }, false);
    doc.addEventListener("DOMContentLoaded", () => {
      recalc();
    }, false);

    recalc();
  })(document, window);
}

const dataURLtoBlob = (dataUrl) => {
  let arr = dataUrl.split(','),
    mime = arr[0].match(/:(.*?);/)[1],
    bstr = atob(arr[1]),
    n = bstr.length,
    u8arr = new Uint8Array(n);
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n);
  }
  return new Blob([u8arr], {
    type: mime,
  });
}

export const downloadBase64 = (data, fileName) => {
  const blobImage = dataURLtoBlob(data);
  const fileOfBlob = new File([blobImage], `${fileName || new Date()}.jpg`);
  const aDom = document.createElement('a');
  aDom.download = fileOfBlob.name;
  let href = URL.createObjectURL(fileOfBlob);
  aDom.href = href;
  document.body.appendChild(aDom);
  aDom.click(); // 触发 a 标签的点击
  document.body.removeChild(aDom);
  URL.revokeObjectURL(href);
}


