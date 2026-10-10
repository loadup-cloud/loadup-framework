<script setup lang="ts">
  import { onMounted, reactive, ref } from 'vue'
  import {
    ElButton,
    ElCard,
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElMessage,
    ElMessageBox,
    ElOption,
    ElPagination,
    ElSelect,
    ElTable,
    ElTableColumn,
    ElTag
  } from 'element-plus'
  import {
    createMerchant,
    updateMerchant,
    getMerchant,
    queryMerchants,
    changeMerchantStatus,
    type Merchant,
    type MerchantInfo,
    type MerchantStatus
  } from '@/api/merchant'

  const rows = ref<Merchant[]>([])
  const total = ref(0)
  const page = ref(1)
  const name = ref('')
  const status = ref<MerchantStatus | ''>('')
  const loading = ref(false)
  const busy = ref(false)
  const dialog = ref(false)
  const selected = ref<Merchant | null>(null)
  type MerchantForm = { [K in keyof MerchantInfo]: NonNullable<MerchantInfo[K]> }
  const blank = (): MerchantForm => ({
    merchantCode: '',
    name: '',
    shortName: '',
    type: 'ENTERPRISE',
    industry: 'RETAIL',
    country: 'CN',
    province: '',
    city: '',
    address: '',
    registrationNo: '',
    contactName: '',
    contactPhone: '',
    contactEmail: ''
  })
  const form = reactive<MerchantForm>(blank())
  const clearPrivate = ref(false)
  const load = async () => {
    loading.value = true
    try {
      const { data, pageInfo } = await queryMerchants({
        name: name.value || undefined,
        status: status.value || undefined,
        page: page.value,
        size: 20
      })
      rows.value = data
      total.value = pageInfo?.totalCount ?? 0
    } catch {
      /* The shared request client displays errors. */
    } finally {
      loading.value = false
    }
  }
  const search = () => {
    page.value = 1
    void load()
  }
  const create = () => {
    selected.value = null
    clearPrivate.value = false
    Object.assign(form, blank())
    dialog.value = true
  }
  const edit = async (row: Merchant) => {
    try {
      const { data } = await getMerchant(row.id)
      selected.value = data
      clearPrivate.value = false
      Object.assign(form, {
        merchantCode: data.merchantCode,
        name: data.name,
        shortName: data.shortName ?? '',
        type: data.type,
        industry: data.industry,
        country: data.country,
        province: data.province ?? '',
        city: data.city ?? '',
        address: '',
        registrationNo: '',
        contactName: '',
        contactPhone: '',
        contactEmail: ''
      })
      dialog.value = true
    } catch {
      /* The shared request client displays errors. */
    }
  }
  const save = async () => {
    if (
      !form.merchantCode ||
      !form.name.trim() ||
      !form.industry ||
      !/^[A-Z]{2}$/.test(form.country)
    ) {
      ElMessage.warning('请填写编码、名称、行业和两位大写国家代码')
      return
    }
    busy.value = true
    try {
      const data: MerchantInfo = { ...form }
      if (selected.value) {
        // Never resubmit masked values. Null preserves existing private fields; empty string clears them.
        for (const key of [
          'address',
          'registrationNo',
          'contactName',
          'contactPhone',
          'contactEmail'
        ] as const)
          data[key] = form[key] || (clearPrivate.value ? '' : null)
        await updateMerchant({
          ...data,
          id: selected.value.id,
          expectedRowVersion: selected.value.rowVersion
        })
      } else await createMerchant(data)
      dialog.value = false
      ElMessage.success('商户资料已保存')
      await load()
    } catch {
      /* The shared request client displays errors. */
    } finally {
      busy.value = false
    }
  }
  const changeStatus = async (row: Merchant) => {
    const target = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    try {
      await ElMessageBox.confirm(
        target === 'INACTIVE'
          ? '停用后，商户的新签约和运行资格检查将被拒绝，历史合约仍保留。'
          : '确认启用商户？',
        '商户状态'
      )
      await changeMerchantStatus(row.id, row.rowVersion, target)
      await load()
    } catch {
      /* Cancellation is normal; the shared client displays request errors. */
    }
  }
  onMounted(load)
</script>

<template>
  <div class="p-20px"
    ><ElCard shadow="never">
      <div class="mb-16px flex flex-wrap items-center gap-10px"
        ><h1 class="mr-auto text-20px">商户信息</h1
        ><ElInput
          v-model="name"
          placeholder="商户名称"
          class="!w-220px"
          clearable
          @keyup.enter="search"
        /><ElSelect v-model="status" placeholder="状态" clearable class="!w-130px"
          ><ElOption label="启用" value="ACTIVE" /><ElOption
            label="停用"
            value="INACTIVE" /></ElSelect
        ><ElButton
          @click="search"
          >查询</ElButton
        ><ElButton type="primary" @click="create">新增商户</ElButton></div
      >
      <p class="mb-12px text-sm opacity-70"
        >商户基本信息供合约资格判断使用，登记信息不代表已完成资质审核。敏感字段仅脱敏展示。</p
      >
      <ElTable :data="rows" v-loading="loading"
        ><ElTableColumn prop="merchantCode" label="商户编码" /><ElTableColumn
          prop="name"
          label="名称"
        /><ElTableColumn prop="type" label="类型" /><ElTableColumn
          prop="industry"
          label="行业"
        /><ElTableColumn prop="country" label="国家" /><ElTableColumn
          prop="contactName"
          label="联系人"
        /><ElTableColumn prop="contactPhone" label="联系电话" /><ElTableColumn label="状态"
          ><template #default="scope"
            ><ElTag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'">{{
              scope.row.status === 'ACTIVE' ? '启用' : '停用'
            }}</ElTag></template
          ></ElTableColumn
        ><ElTableColumn label="操作" width="180"
          ><template #default="scope"
            ><ElButton link type="primary" @click="edit(scope.row)">编辑</ElButton
            ><ElButton
              link
              :type="scope.row.status === 'ACTIVE' ? 'danger' : 'success'"
              @click="changeStatus(scope.row)"
              >{{ scope.row.status === 'ACTIVE' ? '停用' : '启用' }}</ElButton
            ></template
          ></ElTableColumn
        ></ElTable
      >
      <ElPagination
        class="mt-16px"
        v-model:current-page="page"
        :page-size="20"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </ElCard>
    <ElDialog v-model="dialog" :title="selected ? '编辑商户资料' : '新增商户'" width="760px"
      ><ElForm label-width="110px" class="grid gap-x-16px md:grid-cols-2">
        <ElFormItem label="商户编码" required
          ><ElInput v-model="form.merchantCode" :disabled="!!selected" maxlength="64" /></ElFormItem
        ><ElFormItem label="名称" required
          ><ElInput v-model="form.name" maxlength="200" /></ElFormItem
        ><ElFormItem label="简称"><ElInput v-model="form.shortName" maxlength="100" /></ElFormItem
        ><ElFormItem label="类型" required
          ><ElSelect v-model="form.type"
            ><ElOption label="企业" value="ENTERPRISE" /><ElOption
              label="个体工商户"
              value="SELF_EMPLOYED" /><ElOption
              label="个人"
              value="INDIVIDUAL" /></ElSelect></ElFormItem
        ><ElFormItem label="行业代码" required
          ><ElInput v-model="form.industry" maxlength="64" placeholder="如 RETAIL" /></ElFormItem
        ><ElFormItem label="国家代码" required
          ><ElInput v-model="form.country" maxlength="2" placeholder="如 CN" /></ElFormItem
        ><ElFormItem label="省份"><ElInput v-model="form.province" maxlength="64" /></ElFormItem
        ><ElFormItem label="城市"><ElInput v-model="form.city" maxlength="64" /></ElFormItem>
        <template v-if="selected"
          ><p class="md:col-span-2 text-sm opacity-70"
            >原登记号 {{ selected.registrationNo || '未填写' }}；联系人
            {{ selected.contactName || '未填写' }}；电话
            {{ selected.contactPhone || '未填写' }}；邮箱
            {{ selected.contactEmail || '未填写' }}。下方敏感字段留空保留原值，填写则替换。</p
          ><ElFormItem label="空值处理" class="md:col-span-2"
            ><ElSelect v-model="clearPrivate"
              ><ElOption label="保留原敏感字段" :value="false" /><ElOption
                label="清空本次留空的敏感字段"
                :value="true" /></ElSelect></ElFormItem
        ></template>
        <ElFormItem label="地址"><ElInput v-model="form.address" maxlength="500" /></ElFormItem
        ><ElFormItem label="登记号码"
          ><ElInput v-model="form.registrationNo" maxlength="64" /></ElFormItem
        ><ElFormItem label="联系人"
          ><ElInput v-model="form.contactName" maxlength="100" /></ElFormItem
        ><ElFormItem label="联系电话"
          ><ElInput v-model="form.contactPhone" maxlength="32" /></ElFormItem
        ><ElFormItem label="联系邮箱" class="md:col-span-2"
          ><ElInput v-model="form.contactEmail" maxlength="200"
        /></ElFormItem> </ElForm
      ><template #footer
        ><ElButton @click="dialog = false">取消</ElButton
        ><ElButton type="primary" :loading="busy" @click="save">保存</ElButton></template
      ></ElDialog
    ></div
  >
</template>
