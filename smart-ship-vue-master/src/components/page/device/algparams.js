const algparams = {
  // 黑屏检测
  "black_screen": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "黑屏检测",
      btn_name: "标记检测区域",
      sound_text:'黑屏检测告警',
      must: true,
      threshold: 45,
    },
  },
  // 车型识别
  "car_type": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "车型识别",
      btn_name: "标记检测区域",
      sound_text:'车型识别告警',
    },
  },
  // 静电夹
  "collectsplit": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "静电夹检测",
      btn_name: "标记检测区域",
      sound_text:'静电夹检测告警',
    },
  },
  // 灭火器检测
  "extinguisher_misplaced": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "灭火器检测",
      btn_name: "标记灭火器位置",
      sound_text:'灭火器检测告警',
      must: true,
      strategy: 'bottom'
    },
  },
  // 火焰检测
  "fire": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "火焰检测",
      btn_name: "标记检测区域",
      sound_text:'火焰检测告警',
      iou: 0.7
    },
  },
  // 垃圾识别
  "garbage": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 6,
      "threshold":4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "垃圾识别",
      btn_name: "标记检测区域",
      sound_text:'垃圾识别告警',
    },
  },
  // 未佩戴安全帽检测
  "helmet": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未佩戴安全帽检测",
      btn_name: "标记检测区域",
      sound_text:'未佩戴安全帽检测告警',
    },
  },
  // 未佩戴口罩检测
  "mask": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未佩戴口罩检测",
      btn_name: "标记检测区域",
      sound_text:'未佩戴口罩检测告警',
    },
  },
  // 移动侦测
  "motion": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "移动侦测",
      btn_name: "标记检测区域",
      sound_text:'移动侦测告警',
      threshold: 50,
    },
  },
  // 人员离岗检测
  "person_departure": {
    alert_window: {
      "type": "interval_duration_window",
      "duration": 300,
      "interval": 5,
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人员离岗检测",
      btn_name: "标记岗位区域",
      sound_text:'人员离岗检测',
      must: true,
      strategy: 'center'
    },
  },
  // 人员聚集
  "person_gathering": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人员聚集",
      btn_name: "标记检测区域",
      sound_text:'人员聚集告警',
      threshold: 2,
      strategy: 'bottom',
      must:true
    },
  },
  // 区域入侵
  "person_intrusion": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "区域入侵",
      btn_name: "标记入侵区域",
      sound_text:'区域入侵告警',
      must: true,
      strategy: 'bottom'
    },
  },
  // 未穿戴反光衣检测
  "reflective_vest": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未穿戴反光衣检测",
      btn_name: "标记检测区域",
      sound_text:'未穿戴反光衣检测告警',
    },
  },
  // 烟雾检测
  "smog": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "烟雾检测",
      btn_name: "标记检测区域",
      sound_text:'烟雾检测告警',
      iou: 0.7
    },
  },
  // 车辆计数
  "car_counting": {
    alert_window: {
      "type": "threshold_window",
      "length": 1,
      "threshold": 1
    },
    bbox: {
      "lines": []
    },
    reserved_args: {
      ch_name: "车辆计数",
      btn_name: "标记虚拟直线",
      sound_text:'车辆计数告警',
      must: true,
      strategy: 'center'
    },
  },
  // 车辆违停
  "car_misplaced": {
    alert_window: {
      "type": "interval_threshold_window",
      "length": 1,
      "threshold": 1,
      "interval": 5
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "车辆违停",
      btn_name: "标记违停区域",
      sound_text:'车辆违停告警',
      must: true,
      iou: 0.8,
      length: 180,
      threshold: 180,
      strategy: 'center'
    },
  },
  // 电瓶车违停
  "electric_bike_misplaced": {
    alert_window: {
      "type": "interval_threshold_window",
      "length": 1,
      "threshold": 1,
      "interval": 5
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "电瓶车违停",
      btn_name: "标记违停区域",
      sound_text:'电瓶车违停告警',
      must: true,
      iou: 0.6,
      length: 180,
      threshold: 180,
      strategy: 'center'
    },
  },
  // 人员计数
  "person_counting": {
    alert_window: {
      "type": "threshold_window",
      "length": 1,
      "threshold": 1
    },
    bbox: {
      "lines": []
    },
    reserved_args: {
      ch_name: "人员计数",
      btn_name: "标记虚拟直线",
      sound_text:'人员计数告警',
      must: true,
      strategy: 'center'
    },
  },
  // 人员徘徊检测
  "person_loitering": {
    alert_window: {
      "type": "interval_threshold_window",
      "length": 1,
      "threshold": 1,
      "interval": 5
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人员徘徊检测",
      btn_name: "标记检测区域",
      sound_text:'人员徘徊检测告警',
      iou: 0.5,
      length: 180,
      threshold: 120
    },
  },
  // 睡岗检测
  "person_sleep": {
    alert_window: {
      "type": "interval_threshold_window",
      "length": 1,
      "threshold": 1,
      "interval": 5
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "睡岗检测",
      btn_name: "标记检测区域",
      sound_text:'睡岗检测告警',
      iou: 0.9,
      length: 180,
      threshold: 180,
      strategy:'center',
      must:true
    },
  },
  // 大货车计数
  "truck_counting": {
    alert_window: {
      "type": "threshold_window",
      "length": 1,
      "threshold": 1
    },
    bbox: {
      "lines": []
    },
    reserved_args: {
      ch_name: "大货车计数",
      btn_name: "标记虚拟直线",
      sound_text:'大货车计数告警',
      must: true,
      strategy: 'center'
    },
  },
  // 使用手机检测
  "play_phone": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "使用手机检测",
      btn_name: "标记检测区域",
      sound_text:'使用手机检测告警',
      extra_model: {
        play_phone:3
      }
    },
  },
  // 抽烟检测
  "smoke": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "抽烟检测",
      btn_name: "标记检测区域",
      sound_text:'抽烟检测告警',
      extra_model: {
        smoke:3
      }
    },
  },
  // 跌倒
  "fall_down": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 6,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "跌倒检测",
      btn_name: "标记检测区域",
      sound_text:'跌倒检测告警',
      iou: 0.85
    },
  },
  // 未穿工服检测
  "workclothes": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 4
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未穿工服检测",
      btn_name: "标记检测区域",
      sound_text:'未穿工服检测告警',
    },
  },
  // 货架拿取物品动作识别
  "handheld_item": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 0,
      "length": 1,
      "threshold": 1
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "货架拿取物品动作识别",
      btn_name: "标记检测区域",
      sound_text:'货架拿取物品动作识别检测告警',
      threshold: 45,
      must: true,
      extra_model: {
        hrnet:3
      }
    },
  },
  // 人脸识别
  "face": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 1,
      "length": 1,
      "threshold": 1
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人脸识别",
      btn_name: "标记检测区域",
      sound_text:'人脸识别告警',
    },
  },
  // 传送带偏离识别
  "conveyor_belt": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 3,
      "threshold": 2
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "传送带偏离识别",
      btn_name: "标记检测区域",
      sound_text:'传送带偏离识别告警',
      must:true
    },
  },
  // 设备停机检测
  "equipment_shutdown": {
    alert_window: {
      "type": "interval_threshold_window",
      "length": 1,
      "threshold": 1,
      "interval": 5
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "设备停机检测",
      btn_name: "标记检测区域",
      sound_text:'设备停机检测告警',
      diff: 10,
      area: 30,
      length: 60,
      threshold: 60,
      must:true
    },
  },

  
  
  
  // 人员打架检测
  "fight": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人员打架检测",
      btn_name: "标记检测区域",
    },
  },
  // 车牌识别
  "lpr": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "车牌识别",
      btn_name: "标记检测区域",
    },
  },
  // 未穿戴工作服检测
  "workwear_vest_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未穿戴工作服检测",
      btn_name: "标记检测区域",
      must: true
    },
  },
  // 机械设备检测
  "mechanical_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "机械设备检测",
      btn_name: "标记检测区域",
    },
  },
  // 锚杆检测
  "anchor_rod": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "锚杆检测",
      btn_name: "标记检测区域",
    },
  },
  // 吊装作业未设置警戒线检测
  "crane_cordon_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "吊装作业未设置警戒线检测",
      btn_name: "标记检测区域",
    },
  },
  // 挖掘作业未设置警戒线检测
  "excavate_cordon_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "挖掘作业未设置警戒线检测",
      btn_name: "标记检测区域",
    },
  },
  // 吊装作业未设置围栏检测
  "crane_fence_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "吊装作业未设置围栏检测",
      btn_name: "标记检测区域",
    },
  },
  // 挖掘作业未设置围栏检测
  "excavate_fence_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "挖掘作业未设置围栏检测",
      btn_name: "标记检测区域",
    },
  },
  // 人员车辆靠近基坑检测
  "person_car_cross": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "人员车辆靠近基坑检测",
      btn_name: "标记检测区域",
      near_pixel: 100
    },
  },
  // 挖掘机旋转半径有人检测
  "excavate_person_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "挖掘机旋转半径有人检测",
      btn_name: "标记检测区域",
      rotation_radius: 1
    },
  },
  // 吊车旋转半径有人检测
  "crane_person_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "吊车旋转半径有人检测",
      btn_name: "标记检测区域",
    },
  },
  // 未设置逃生通道检测
  "no_ladder_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未设置逃生通道检测",
      btn_name: "标记检测区域",
    },
  },
  // 未穿戴安全背带检测
  "safe_belt_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未穿戴安全背带检测",
      btn_name: "标记检测区域",
    },
  },
  // 未放坡检测
  "grading_det": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "未放坡检测",
      btn_name: "标记检测区域",
    },
  },
  // 管线站人检测
  "pipeline": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "管线站人检测",
      btn_name: "标记检测区域",
    },
  },
  // 驾驶室人员检测
  "manned": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "驾驶室人员检测",
      btn_name: "标记检测区域",
    },
  },
  // 吊瓶吊装检测
  "acetylene_cylinder": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "吊瓶吊装检测",
      btn_name: "标记检测区域",
    },
  },
  // 高空作业未挂挂钩
  "high_hanging_low": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "高空作业未挂挂钩",
      btn_name: "标记检测区域",
    },
  },
  // 大货车未盖篷布检测
  "tarpaulin": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "大货车未盖篷布检测",
      btn_name: "标记检测区域",
      must: true
    },
  },
  // 扬尘检测
  "dust": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "扬尘检测",
      btn_name: "标记检测区域",
    },
  },
  // 异物堵塞检测
  "blockage": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "异物堵塞检测",
      btn_name: "标记检测区域",
    },
  },
  // 动物(猪)离位检测
  "pig_departure": {
    alert_window: {
      "type": "duration_window",
      "duration": 300,
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "动物(猪)离位检测",
      btn_name: "标记检测区域",
      must: true
    },
  },
  // 动物(猪)越界检测
  "pig_intrusion": {
    alert_window: {
      "type": "interval_threshold_window",
      "interval": 5,
      "length": 5,
      "threshold": 3
    },
    bbox: {
      "polygons": []
    },
    reserved_args: {
      ch_name: "动物(猪)越界检测",
      btn_name: "标记检测区域",
      must: true
    },
  },
}

export const getAlgDefaultParams = (algname) => {
  if (algparams[algname]) {
    return JSON.parse(JSON.stringify(algparams[algname]))
  } else {
    return null
  }
}