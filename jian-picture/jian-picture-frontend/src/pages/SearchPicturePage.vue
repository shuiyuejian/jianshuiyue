<template>
  <div id="searchPicturePage">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin-bottom: 16px">原图</h3>
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
          style="height: 180px; object-fit: cover"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">识图结果</h3>
    <a-alert
      type="warning"
      show-icon
      message="以图搜图功能暂不可用"
      description="该功能依赖的外部识图接口已被封禁，暂时无法提供图片搜索服务，敬请期待。"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getPictureVoByIdUsingGet } from '@/api/pictureController.ts'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const pictureId = computed(() => {
  return route.query?.pictureId
})
const picture = ref<API.PictureVO>({})

// 获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchPictureDetail()
})
</script>

<style scoped>
#searchPicturePage {
  margin-bottom: 16px;
}
</style>
