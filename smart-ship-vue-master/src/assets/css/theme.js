const themes = {
  black: {
    mainText: "rgba(255,255,255,0.6)",
    mainActiveText: "rgba(255,255,255,0.8)",
    chartTooltipBg: "rgba(255,255,255,0.2)",

    menuText: "rgba(255,255,255,0.6)",
    menuActiveText: "#409eff",
    menuActiveLeft: "#409EFF",
    menuBg: '#2f3037',
    menuHoverBg: 'rgba(255, 255, 255, 0.1)',
    menuPopperBg: "#303133",
    subMenuBg: '#23242b',
    submenuHoverBg: 'rgba(255, 255, 255, 0.1)',

    MaincontainerBg: '#060A18',
    mainBg: "#f3f3f4",
    inputBg: "#2A2B33",
    searchPrimaryBtnBg: "#2A2B33",
    selDropdownBg: "#2A2B33",
    
    tablecontainerbg: 'rgb(26 29 36 / 90%)',
    tableheaderBg: '#2F3037',
    tableHoverBg: '#2F3037',
    tableStripedBg: "#2A2B33",
    tablesplit: '#666',
    tableColor: 'rgba(255,255,255,0.6)',

    btnprimary: '#2C6DD2',
    btncolor: "#fff",
    btnprimaryhover: '#66b1ff',

    paginationColor: 'rgba(255, 255, 255, 0.6)',
    paginationBorderColor: '#605f5f',
    paginationBg: '#23242b', 
    paginationHoverBg: "rgba(255, 255, 255, 0.1)",

    dialogbg: '#23242B',
    dialogSplit: '#484951',
    placeholder: '#666',

    selActiveBg:"#e5e5e5",
    selActiveText:"#4e6ef2",

    screenline: '#00c2ff'
  },
  white: {
    mainText: "#333",
    mainActiveText: "#666",
    chartTooltipBg: "rgba(0,0,0,0.4)",

    menuText: "#aaa",
    menuActiveText: "#fff",
    menuActiveLeft: "#2C6DD2",
    menuBg: '#041d2f',
    menuHoverBg: '#0e283a',
    menuPopperBg: "#0e283a",
    subMenuBg: '#041d2f',
    submenuHoverBg: '#0e283a',
    
    MaincontainerBg: '#fff',
    mainBg: "#f3f3f4",
    inputBg: "#f0f1f3",
    searchPrimaryBtnBg: "#f0f1f3",
    // selDropdownBg: "#f0f1f3",
    selDropdownBg: "#fff",

    tablecontainerbg: '#e5e5e5',
    // tableheaderBg: 'rgb(166 203 241 / 20%)',
    tableheaderBg: '#f5f5f5',
    tableHoverBg: '#f7f7f7',
    // tableStripedBg: "#f2f9ff",
    tableStripedBg: "#f6f6f6",
    tablesplit: '#999',
    tableColor: '#333',

    btnprimary: '#2C6DD2',
    btncolor: "#fff",
    btnprimaryhover: '#66b1ff',

    paginationColor: '#333',
    paginationBorderColor: '#ccc',
    paginationBg: '#fff', 
    paginationHoverBg: "#e5e5e5",

    dialogbg: '#fff',
    dialogSplit: '#e5e5e5',
    placeholder: '#ccc',

    selActiveBg:"#e5e5e5",
    selActiveText:"#4e6ef2",

    screenline: '#00c2ff'
  },
};


// 修改页面中的样式变量值
const changeStyle = (obj) => {
  for (let key in obj) {
    document
      .getElementsByTagName("body")[0]
      .style.setProperty(`--${key}`, obj[key]);
  }
};
// 改变主题的方法
export const setTheme = (themeName) => {
  localStorage.setItem("theme", themeName); // 保存主题到本地，刷新页面时使用该主题

  const themeConfig = themes[themeName];
  changeStyle(themeConfig); // 改变样式
};