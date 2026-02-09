// 获取图片完整URL
export function getImageUrl(path) {
  if (!path) return ''
  if (path.startsWith('http')) return path
  // 已经是完整的api路径
  if (path.startsWith('/api/')) {
    return path
  }
  // 本地静态图片路径
  if (path.startsWith('/images/')) {
    return `/api${path}`
  }
  // 上传的文件路径 (如 /file/xxx.png)
  if (path.startsWith('/file/')) {
    return `/api${path}`
  }
  // 其他路径
  return `/api${path.startsWith('/') ? '' : '/'}${path}`
}
