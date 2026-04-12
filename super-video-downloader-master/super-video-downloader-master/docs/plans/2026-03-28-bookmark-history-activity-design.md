# Bookmark History Activity Design

**Date:** 2026-03-28

**Status:** Approved for implementation

## Goal

将书签页和历史页从当前叠加在浏览器容器中的 Fragment 改为独立 Activity，并在用户点击条目后把目标 URL 返回给浏览器首页，直接覆盖当前选中的浏览标签页。书签页和历史页 UI 参考 Figma 节点 `37:154` 与 `14:73`，忽略顶部时间、电量等状态栏区域。

## Current State

- 书签页目前由 [`BookmarksFragment`](../../app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksFragment.kt) 承载，通过 [`BaseWebTabFragment`](../../app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt) 使用 `FragmentTransaction` 叠加到 `fragment_container_view` 上。
- 历史页目前由 [`HistoryFragment`](../../app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryFragment.kt) 承载，同样通过浏览器容器叠加展示。
- 点击书签条目时，当前逻辑调用 `browserServicesProvider.getOpenTabEvent()` 新开一个网页标签页。
- 点击历史条目时，当前逻辑调用 `BrowserViewModel.instance?.openPageEvent` 新开一个网页标签页。
- 浏览器当前把第 0 个标签位当作固定 Home tab。[`BrowserTabSessionManager`](../../app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt) 会在 `sanitize()` 中强制首个标签为 Home。

## UX Requirements

### Shared behavior

- 书签页和历史页都必须是独立 Activity，而不是叠加 Fragment。
- 从浏览器首页快捷入口和网页标签菜单进入时，都打开对应 Activity。
- 点击列表项后关闭 Activity，并把 URL 返回给浏览器。
- 浏览器收到返回值后，不新开 tab，而是覆盖当前选中的 tab。
- 如果当前选中的正好是 Home tab，也允许被普通网页 tab 覆盖。

### Bookmark page

- 参考 Figma `37:154`。
- 页面背景色为 `#F5F5F5`。
- 顶部为返回按钮、居中标题 `Bookmark`、右侧搜索按钮。
- 主体为白色圆角 12dp 卡片容器，内部显示书签列表。
- 列表行左侧为 32dp 圆角图标占位，右侧为标题和域名两行文案，最右为浅灰色箭头。
- 底部右下角保留黄色新增悬浮按钮。

### History page

- 参考 Figma `14:73`。
- 顶部结构和书签页一致，标题为 `History`。
- 主体为白色圆角 12dp 卡片容器，内部显示历史记录列表。
- 底部保留独立的 `Clear History` 按钮区。

## Technical Approach

### Navigation model

- 新增 `BookmarksActivity` 与 `HistoryActivity`，分别承载书签页和历史页。
- 浏览器端改用 `ActivityResultLauncher<Intent>` 打开 Activity，而不是 `FragmentTransaction`。
- Activity 使用 `setResult(RESULT_OK, intent)` 返回所选 URL。

### Result contract

- 为两个 Activity 提供统一结果协议：
  - `EXTRA_SELECTED_URL`
  - 可选 `EXTRA_SELECTED_TITLE`
- 浏览器端新增“覆盖当前标签页”的事件通道，不复用当前“新开 tab”的 `openPageEvent`。

### Browser tab replacement

- 浏览器现有 `openPageEvent` 语义是“新开标签页”。
- 新增“替换当前标签页”的事件或方法，用于把当前选中 tab 替换为目标 URL 对应的普通网页 tab。
- 替换逻辑必须覆盖两种场景：
  - 当前为普通网页 tab：保留位置与选中状态，替换 URL/title/id。
  - 当前为 Home tab：将该槽位直接替换为普通网页 tab，不再强制首页常驻。

### Home tab constraint relaxation

- 当前 [`BrowserTabSessionManager`](../../app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt) 的 `sanitize()` 会强制首项为 Home tab。
- 为满足“首页 tab 可被覆盖”，需要调整该约束：
  - 默认会话仍以 Home tab 启动。
  - 但后续不强制首项必须是 Home。
  - Home 与普通网页 tab 的判断改为 `tab.isHome()`，而不是“index == 0”。

## Affected Areas

### New files

- `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksActivity.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryActivity.kt`
- `app/src/main/res/layout/activity_bookmarks.xml`
- `app/src/main/res/layout/activity_history.xml`

### Existing files likely to change

- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserViewModel.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksFragment.kt`
- `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryFragment.kt`
- `app/src/main/res/layout/fragment_bookmarks.xml`
- `app/src/main/res/layout/fragment_history.xml`
- 相关颜色、drawable、string 资源文件

## Non-Goals

- 不重写书签与历史的数据仓储逻辑。
- 不改变书签新增、搜索、删除、排序等核心业务规则。
- 不在这次改动中处理无关浏览器菜单、代理、帮助页导航。

## Risks

- Home tab 的固定假设存在于浏览器多个位置，若漏改可能导致首页和普通页切换异常。
- 当前书签和历史 Fragment 直接依赖宿主环境，若直接迁 Activity 需要谨慎拆开 `MainActivity` 耦合。
- 现有工程存在 Data Binding 编译问题，可能影响本次最终验证。

## Verification Strategy

- 单元测试覆盖浏览器标签会话更新逻辑，重点验证：
  - 普通 tab 被覆盖。
  - Home tab 被覆盖。
  - 替换后当前选中索引正确。
- 单元测试覆盖 Activity 结果处理逻辑，确认书签页和历史页返回不会新开 tab。
- 手工验证：
  - 从首页进入书签页并点击条目，当前 tab 被覆盖。
  - 从首页进入历史页并点击条目，当前 tab 被覆盖。
  - 从网页标签菜单进入书签/历史页并点击条目，当前网页 tab 被覆盖。
  - 历史页清空按钮、书签页新增按钮、搜索与空状态视觉效果符合 Figma。
