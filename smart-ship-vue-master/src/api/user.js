import request from "@/utils/request";
import { projConfig } from "@/utils/config";
// import { getconfig } from "./common.js"
import { encryptWithAes, getEncryptData } from "@/utils/cryptoUtils.js";

let api = projConfig.curBaseUrl;
let encryptKey = "K#20a20i20c23v@S";

// getconfig().then(res => {
//   encryptKey = res.key;
// })

export const user = {
  getTableData: () => {
    return request.get(`${api}/user`);
  },
  add: (data) => {
    return request.post(`${api}/user`, {
      data: encrypted(JSON.stringify(data)),
    });
  },
  edit: (data) => {
    return request.put(`${api}/user`, {
      data: encrypted(JSON.stringify(data)),
    });
  },

  del: (data) => {
    return request.delete(`${api}/user`, {
      data: { data: encrypted(JSON.stringify(data)) },
    });
  },

  login: (userInfo) => {
    // request.defaults.headers.post['encrypt-key'] = getEncryptData();
    // request.defaults.headers.post['is-encrypt'] = true;
    // request.defaults.headers.post['is-token'] = false;
    // request.defaults.headers.post['repeat-submit'] = false;
    const params = {
      ...userInfo,
      clientId: "e5cd7e4891bf95d1d19206ce24a7b32e",
      grantType: "password",
      rememberMe: false,
      tenantId: "000000",
    };

    return request.post(
      `${api}/auth/login`,
      encryptWithAes(JSON.stringify(params)),
      {
        headers: {
          "encrypt-key": getEncryptData(),
          "is-encrypt": true,
          "is-token": false,
          "repeat-submit": false,
          "Content-Type": "application/json",
        },
      }
    );
  },
  userInfo: () => {
    return request.get(`${api}/system/user/getInfo`);
  },
  getRouterList: () => {
    return request.get(`${api}/system/menu/getRouters`);
  },
  // 修改密码
  chgpwd: (data) => {
    return request.put(
      `${api}/system/user/profile/updatePwd`,
      encryptWithAes(JSON.stringify(data)),
      {
        headers: {
          "encrypt-key": getEncryptData(),
          "is-encrypt": true,
          "is-token": false,
          "repeat-submit": false,
          "Content-Type": "application/json",
        },
      }
    );
  },
  // admin账号校验密保
  checkhint: (data) => {
    return request.post(`${api}/user/hint`, data);
  },

  // 忘记密码获取admin账号hint
  gethint: (params) => {
    return request.get(`${api}/user/hint`, { params });
  },

  updateToken: () => {
    return request.get(`/${api}/user/token_by_token`);
  },

  getToken: () => {
    return request.get(`/${api}/user/token`);
  },

  getCaptcha: () => {
    return request.get(`/${api}/user/login/captcha`);
  },

  getAccessInfo: () => {
    return request.get(`/${api}/user/access_key`);
  },
};

export const getSysTime = () => {
  return request.get(`${api}/system/time`);
};

export const encrypted = (text) => {
  let encrypt = CryptoJS.AES.encrypt(
    text,
    CryptoJS.enc.Utf8.parse(encryptKey),
    {
      mode: CryptoJS.mode.ECB,
      padding: CryptoJS.pad.Pkcs7,
    }
  );
  return encrypt.toString();
};

export const generate_signature = (sk, ak, timestamp, nonce, data) => {
  var ak = "66b1dc29ca6e4ecae84b0262";
  var sk = "17750041-c10f-4e61-8011-3576271cf5f5";
  var timestamp = 1722995536;
  var nonce = "1722995536";
  var message = `${ak}:${timestamp}:${nonce}`;
  var hash = CryptoJS.HmacSHA256(message, sk);
  var str_encode = CryptoJS.enc.Hex.stringify(hash);
  return;
};
