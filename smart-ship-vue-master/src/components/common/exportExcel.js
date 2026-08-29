export const exportExcel = {
  csv2sheet: (csv) => {
    var sheet = {};
    csv = csv.split('\n');
    csv.forEach(function (row, i) {
      row = row.split(',');
      if (i == 0) sheet['!ref'] = 'A1:' + String.fromCharCode(65 + row.length - 1) + (csv.length - 1);
      row.forEach(function (col, j) {
        sheet[String.fromCharCode(65 + j) + (i + 1)] = { v: col };
      });
    });
    return sheet;
  },
  json2sheet: (json) => {
    var sheet = {};
    json.forEach(function (item, i) {
      // let row = Object.values(item)
      let row = item;
      if (i == 0) sheet['!ref'] = 'A1:' + String.fromCharCode(65 + row.length - 1) + (json.length);
      row.forEach(function (col, j) {
        sheet[String.fromCharCode(65 + j) + (i + 1)] = { v: col };
        sheet[String.fromCharCode(65 + j) + (i + 1)]['s'] = {
          alignment: {
            // horizontal: 'center',
            vertical: 'center', 
            wrapText: true
          }
        }
      });
    });
    return sheet;
  },
  sheet2blob: (sheet, sheetName, cols, rows) => {
    sheetName = sheetName || 'sheet1';
    var workbook = {
      SheetNames: [sheetName],
      Sheets: {}
    };
    workbook.Sheets[sheetName] = sheet;
    if (cols) {
      workbook.Sheets[sheetName]["!cols"] = cols
    }
    if (rows) {
      workbook.Sheets[sheetName]["!rows"] = rows
    }
    // if(pic){
    //   const drawinga=workbook.Sheets[sheetName].addDrawing(picture);
    //   drawinga.setDrawingPosition(3,3,8,8);
    // }
    var wopts = {
      bookType: 'xlsx',
      bookSST: false, // 是否生成Shared String Table，官方解释是，如果开启生成速度会下降，但在低版本IOS设备上有更好的兼容性
      type: 'binary'
    };
    var wbout = XLSX.write(workbook, wopts);
    var blob = new Blob([s2ab(wbout)], { type: "application/octet-stream" });
    // 字符串转ArrayBuffer
    function s2ab(s) {
      var buf = new ArrayBuffer(s.length);
      var view = new Uint8Array(buf);
      for (var i = 0; i != s.length; ++i) view[i] = s.charCodeAt(i) & 0xFF;
      return buf;
    }
    return blob;
  },
  openDownloadDialog: (url, saveName) => {
    if (typeof url == 'object' && url instanceof Blob) {
      url = URL.createObjectURL(url); // 创建blob地址
    }
    var aLink = document.createElement('a');
    aLink.href = url;
    aLink.download = saveName || ''; // HTML5新增的属性，指定保存文件名，可以不要后缀，注意，file:///模式下不会生效
    var event;
    if (window.MouseEvent) event = new MouseEvent('click');
    else {
      event = document.createEvent('MouseEvents');
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
    }
    aLink.dispatchEvent(event);
  }
}