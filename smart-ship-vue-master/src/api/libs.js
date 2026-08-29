import request from '@/utils/request';
import { projConfig } from "@/utils/config";

let api = projConfig.curBaseUrl;

export const group = {
  getGroupList: (params) => {
    return request.get(`${api}/group`, { params })
  },
  add: (data) => {
    return request.post(`${api}/group`, data)
  },
  edit: (data) => {
    return request.put(`${api}/group`, data)
  },
  save: (data) => {
    if (data.id) {
      data.device_id = data.device_ids[0];
      delete data.device_ids;

    } else {

    }
  },
  del: (data) => {
    return request.delete(`${api}/group`, { data })
  }
}

export const facelibApis = {
  getTableData: (params) => {
    return request.get(`${api}/face`, { params })
  },
  save: (obj) => {
    if (obj.id) {
      return request.put(`${api}/face`, obj)
    } else {
      return request.post(`${api}/face`, obj)
    }
  },
  saveimg: (obj) => {
    let formData = new FormData();
    formData.append("id", obj.id);
    formData.append("device_ids", JSON.stringify(obj.device_ids));
    formData.append("image", obj.image);
    return request.put(`${api}/face/image`, formData)
  },
  del: (data) => {
    return request.delete(`${api}/face`, { data })
  },
  facesync: (data) => {
    return request.post(`/facesync/face/sync`, data)
  },
  faceload: (data) => {
    return request.post(`${api}/face/load`, data)
  },
  import: (data) => {
    return request.post(`${api}/face/xlsx`, data)
  },
  export: (params) => {
    return request.get(`${api}/face/xlsx`, { params, responseType: 'blob' })
  }
}

export const workclothesApis = {
  getTableData: (params) => {
    return request.get(`${api}/work_clothes`, { params })
  },
  save: (obj) => {
    if (obj.id) {

      return request.put(`${api}/work_clothes`, obj)
    } else {
      return request.post(`${api}/work_clothes`, obj)
    }
  },
  saveimg: (obj) => {
    let formData = new FormData();
    formData.append("id", obj.id);
    // formData.append("force", obj.force);
    formData.append("device_ids", JSON.stringify(obj.device_ids));
    formData.append("image", obj.image);
    return request.put(`${api}/work_clothes/image`, formData)
  },
  del: (data) => {
    return request.delete(`${api}/work_clothes`, { data })
  },
  load: (data) => {
    return request.post(`${api}/work_clothes/load`, data)
  },
  import: (data) => {
    return request.post(`${api}/work_clothes/xlsx`, data)
  },
  export: (params) => {
    // device_id
    return request.get(`${api}/work_clothes/xlsx`, { params, responseType: 'blob' })
  }
}

export const ppeApis = {
  getTableData: (params) => {
    return request.get(`${api}/ppe`, { params })
  },
  save: (obj) => {
    if (obj.id) {
      return request.put(`${api}/ppe`, obj)
    } else {
      return request.post(`${api}/ppe`, obj)
    }
  },
  saveimg: (obj) => {
    let formData = new FormData();
    formData.append("id", obj.id);
    formData.append("device_ids", JSON.stringify(obj.device_ids));
    formData.append("image", obj.image);
    return request.put(`${api}/ppe/image`, formData)
  },
  del: (data) => {
    return request.delete(`${api}/ppe`, { data })
  },
  load: (data) => {
    return request.post(`${api}/ppe/load`, data)
  },
  import: (data) => {
    return request.post(`${api}/ppe/xlsx`, data)
  },
  export: (params) => {
    // device_id, category
    return request.get(`${api}/ppe/xlsx`, { params, responseType: 'blob' })
  }
}

export const openlibApis = {
  getTableData: (params) => {
    return request.get(`${api}/open_lib`, { params })
  },
  save: (obj) => {
    if (obj.id) {
      return request.put(`${api}/open_lib`, obj)
    } else {
      return request.post(`${api}/open_lib`, obj)
    }
  },
  saveimg: (obj) => {
    let formData = new FormData();
    formData.append("id", obj.id);
    formData.append("device_ids", JSON.stringify(obj.device_ids));
    formData.append("image", obj.image);
    return request.put(`${api}/open_lib/image`, formData)
  },
  del: (data) => {
    return request.delete(`${api}/open_lib`, { data })
  },
  load: (data) => {
    return request.post(`${api}/open_lib/load`, data)
  },
  import: (data) => {
    return request.post(`${api}/open_lib/xlsx`, data)
  },
  export: (params) => {
    // device_id, category
    return request.get(`${api}/open_lib/xlsx`, { params, responseType: 'blob' })
  }
}