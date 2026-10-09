<script setup lang="ts">
  import { ElAlert } from 'element-plus'
</script>

<template>
  <ElAlert
    type="info"
    :closable="false"
    show-icon
    class="mb-18px"
    title="目录草稿保存到服务端后才会持久化；关联项需先发布。已发布版本不可修改。签约预览使用服务端结果，正式签约需单独确认。"
  />
</template>
