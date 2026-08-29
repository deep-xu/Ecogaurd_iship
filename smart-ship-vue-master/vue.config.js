'use strict'
const path = require('path')
const webpack = require('webpack')

function resolve(dir) {
  return path.join(__dirname, dir)
}
module.exports = {
  publicPath: './',
  productionSourceMap: false,
  devServer: {
    port: 8082,
    proxy: {
      '/api': {
        target: 'http://localhost:8081/',
        //target: process.env.NODE_ENV === 'development' ? 'http://47.98.183.192:8081/' : 'http://47.98.183.192:8081/',
        //target: process.env.NODE_ENV === 'development' ? 'http://localhost:8080/' : 'http://localhost:8080/',
        changeOrigin: true,
        pathRewrite: {
          '/api': ''
        }
      },
      '/ks': {
        target: 'http://localhost:8081/',
        changeOrigin: true,
        pathRewrite: {
            '/ks': ''
        }
      },
      '/stream': {
        target: "http://18.163.187.146:9189/ks/proxy/stream",
        changeOrigin: true,
        pathRewrite: {
            '/stream': ''
        }
      },
      '/monitorstatus': {
        target: "http://18.163.187.146:9190/ks/proxy/",
        changeOrigin: true,
        pathRewrite: {
            '/monitorstatus': ''
        }
      },
      '/facesync': {
        target: "http://18.163.187.146:9190/ks/proxy",
        changeOrigin: true,
        pathRewrite: {
            '/facesync': ''
        }
      },
      'staticdata/': {
        target: 'http://47.98.238.123:9192/staticdata',
        changeOrigin: true,
        pathRewrite: {
          'staticdata/': ''
        }
      },
      'algsjson/': {
        target: 'http://18.163.187.146:9192/algsjson/',
        changeOrigin: true,
        pathRewrite: {
          'algsjson/': ''
        }
      }
    }
  },
  configureWebpack: {
    devtool: process.env.NODE_ENV === 'dev' ? 'source-map' : undefined,
    resolve: {
      alias: {
        '@': resolve('/src')
      }
    }
  },
  chainWebpack: config => {
    config.plugin('html')
      .tap(args => {
        args[0].title = "Intelligent Management Platform";
        return args;
      })
    // 打包分包
    config.optimization.splitChunks({
      cacheGroups: {
        common: {
          name: 'chunk-common', // 打包后的文件名
          chunks: 'initial', // 
          minChunks: 2,
          maxInitialRequests: 5,
          minSize: 0,
          priority: 1,
          reuseExistingChunk: true
        },
        vendors: {
          name: 'chunk-vendors',
          test: /[\\/]node_modules[\\/]/,
          chunks: 'initial',
          priority: 2,
          reuseExistingChunk: true,
          enforce: true
        },
        elementuiVue: {
          name: 'chunk-elementui-vue',
          test: /[\\/]node_modules[\\/]element-ui[\\/]/,
          chunks: 'initial',
          priority: 3,
          reuseExistingChunk: true,
          enforce: true
        },
        echartsVue: {
          name: 'chunk-echarts-vue',
          test: /[\\/]node_modules[\\/]echarts[\\/]/,
          chunks: 'initial',
          priority: 3,
          reuseExistingChunk: true,
          enforce: true
        }
      }
    })
  },
  pluginOptions: {
    "style-resources-loader": {
      preProcessor: "less",
      patterns: [
        // 这个是加上自己的路径,不能使用(如下:alias)中配置的别名路径
        path.resolve(__dirname, "./src/assets/css/variables.less"),
      ],
    },
  },
}