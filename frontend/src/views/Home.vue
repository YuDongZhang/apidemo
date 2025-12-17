<template>
  <div class="home-container">
    <div class="welcome-card glass-card fade-in">
      <h1 class="gradient-text">欢迎使用 API Demo 管理系统</h1>
      <p>请从左侧选择栏目，或点击功能入口进行操作</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.home-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}

.welcome-card {
  padding: 40px;
  text-align: center;
}

.welcome-card h1 {
  font-size: 1.8rem;
  margin-bottom: 16px;
}

.welcome-card p {
  color: #666;
}
</style>
