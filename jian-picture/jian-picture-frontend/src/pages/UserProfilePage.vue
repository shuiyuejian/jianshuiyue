<template>
  <div id="userProfilePage">
    <a-card title="个人资料" style="max-width: 640px; margin: 0 auto">
      <a-form :model="formData" layout="vertical" @finish="doUpdate">
        <!-- 只读字段 -->
        <a-form-item label="用户 ID">
          <a-input :value="loginUserStore.loginUser.id" disabled />
        </a-form-item>
        <a-form-item label="账号">
          <a-input :value="loginUserStore.loginUser.userAccount" disabled />
        </a-form-item>
        <a-form-item label="用户角色">
          <a-tag v-if="loginUserStore.loginUser.userRole === 'admin'" color="gold">管理员</a-tag>
          <a-tag v-else color="blue">普通用户</a-tag>
        </a-form-item>
        <!-- 可编辑字段 -->
        <a-form-item label="用户名">
          <a-input v-model:value="formData.userName" placeholder="请输入用户名" :maxlength="16" />
        </a-form-item>
        <a-form-item label="头像">
          <a-upload
            class="avatar-upload"
            list-type="picture-card"
            :show-upload-list="false"
            :custom-request="handleUpload"
            :before-upload="beforeUpload"
          >
            <img v-if="formData.userAvatar" :src="formData.userAvatar" alt="avatar" />
            <div v-else>
              <loading-outlined v-if="uploading"></loading-outlined>
              <plus-outlined v-else></plus-outlined>
            </div>
          </a-upload>
          <div style="margin-top: 12px">
            <a-input v-model:value="formData.userAvatar" placeholder="或粘贴头像图片 URL" allow-clear />
          </div>
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea
            v-model:value="formData.userProfile"
            placeholder="介绍一下自己吧"
            :rows="3"
            :maxlength="256"
            show-count
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting">保存</a-button>
          <a-button style="margin-left: 12px" @click="openPasswordModal">修改密码</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 修改密码弹窗 -->
    <a-modal
      v-model:open="passwordModalOpen"
      title="修改密码"
      :confirm-loading="passwordSubmitting"
      ok-text="确认修改"
      cancel-text="取消"
      @ok="doUpdatePassword"
    >
      <a-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" layout="vertical">
        <a-form-item label="旧密码" name="oldPassword">
          <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入旧密码" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password
            v-model:value="passwordForm.newPassword"
            placeholder="请输入新密码（至少 8 位）"
          />
        </a-form-item>
        <a-form-item label="确认新密码" name="checkPassword">
          <a-input-password
            v-model:value="passwordForm.checkPassword"
            placeholder="请再次输入新密码"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { LoadingOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { FormInstance, UploadProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { updateMyPasswordUsingPost, updateMyUserUsingPost } from '@/api/userController.ts'
import { uploadAvatarUsingPost } from '@/api/fileController.ts'

const loginUserStore = useLoginUserStore()
const router = useRouter()

const formData = reactive<API.UserUpdateMyRequest>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const submitting = ref<boolean>(false)
const uploading = ref<boolean>(false)

onMounted(() => {
  const user = loginUserStore.loginUser
  if (!user.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }
  formData.userName = user.userName ?? ''
  formData.userAvatar = user.userAvatar ?? ''
  formData.userProfile = user.userProfile ?? ''
})

// 保存资料
const doUpdate = async () => {
  if (!formData.userName?.trim()) {
    message.error('用户名不能为空')
    return
  }
  if (!formData.userAvatar?.trim()) {
    message.error('请上传或填写头像')
    return
  }
  submitting.value = true
  try {
    const res = await updateMyUserUsingPost({
      userName: formData.userName,
      userAvatar: formData.userAvatar,
      userProfile: formData.userProfile,
    })
    if (res.data.code === 0) {
      message.success('保存成功')
      // 刷新全局登录用户信息，顶部头像昵称即时更新
      await loginUserStore.fetchLoginUser()
    } else {
      message.error('保存失败，' + res.data.message)
    }
  } catch (error) {
    message.error('保存失败，' + (error as any).message)
  } finally {
    submitting.value = false
  }
}

// 上传头像
const handleUpload = async ({ file }: any) => {
  uploading.value = true
  try {
    const res = await uploadAvatarUsingPost({}, file)
    if (res.data.code === 0 && res.data.data) {
      formData.userAvatar = res.data.data
      message.success('头像上传成功')
    } else {
      message.error('头像上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('头像上传失败，' + (error as any).message)
  } finally {
    uploading.value = false
  }
}

// 修改密码相关
const passwordModalOpen = ref<boolean>(false)
const passwordSubmitting = ref<boolean>(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive<API.UserUpdateMyPasswordRequest>({
  oldPassword: '',
  newPassword: '',
  checkPassword: '',
})
const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码' }],
  newPassword: [
    { required: true, message: '请输入新密码' },
    { min: 8, message: '新密码长度不能小于 8 位' },
  ],
  checkPassword: [
    { required: true, message: '请再次输入新密码' },
    {
      validator: (_: any, value: string) => {
        if (!value || value === passwordForm.newPassword) {
          return Promise.resolve()
        }
        return Promise.reject(new Error('两次输入的密码不一致'))
      },
    },
  ],
}

// 打开修改密码弹窗并清空表单
const openPasswordModal = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.checkPassword = ''
  passwordModalOpen.value = true
}

// 提交修改密码
const doUpdatePassword = async () => {
  try {
    await passwordFormRef.value?.validate()
  } catch {
    return
  }
  passwordSubmitting.value = true
  try {
    const res = await updateMyPasswordUsingPost({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      checkPassword: passwordForm.checkPassword,
    })
    if (res.data.code === 0) {
      message.success('密码修改成功，请重新登录')
      passwordModalOpen.value = false
      // 后端已清除登录态，前端重置本地状态并跳转登录页
      loginUserStore.setLoginUser({ userName: '未登录' })
      await router.push('/user/login')
    } else {
      message.error('修改失败，' + res.data.message)
    }
  } catch (error) {
    message.error('修改失败，' + (error as any).message)
  } finally {
    passwordSubmitting.value = false
  }
}

// 上传前的校验
const beforeUpload = (file: UploadProps['fileList'][number]) => {
  const isJpgOrPng =
    file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  if (!isJpgOrPng) {
    message.error('不支持上传该格式的图片，推荐 jpg、png 或 webp')
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('不能上传超过 10M 的图片')
  }
  return isJpgOrPng && isLt10M
}
</script>
<style scoped>
#userProfilePage .avatar-upload :deep(.ant-upload) {
  width: 96px !important;
  height: 96px !important;
}

#userProfilePage .avatar-upload img {
  max-width: 100%;
  max-height: 100%;
}
</style>
