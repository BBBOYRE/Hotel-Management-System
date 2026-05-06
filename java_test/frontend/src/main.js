const loadBtn = document.querySelector("#loadBtn");
const result = document.querySelector("#result");

async function loadVersion() {
  result.textContent = "请求中...";
  try {
    const response = await fetch("/api/version");
    const data = await response.json();
    result.textContent = JSON.stringify(data, null, 2);
  } catch (error) {
    result.textContent = `请求失败: ${error.message}`;
  }
}

loadBtn.addEventListener("click", loadVersion);
