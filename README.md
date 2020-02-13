# wyq_github_appbar_layout_behavior
仿淘宝和58首页, 使用CoordinatorLayout和AppbarLayout 来实现

目前解决了
1. appbarLayout的回弹问题
2. appbarLayout的滑动点击无法暂停
3. 头部appbarLayout的惯性滑动未消费的部分传递给下面的列表继续滑动



在自定义behavior中
public class BothDoScrollBehavior extends CoordinatorLayout.Behavior<View> 
  
  
 处理位置:
layoutDependsOn() 返回true, 就会调用onDependentViewChanged()这个回调中可以设置位置
target是目标发起者, child是Behavior在布局中赋值的自己


处理滚动:
onStartNestedScroll 返回true表示跟踪滑动事件
onNestedScroll 滚动回调 
onNestedFling 惯性滑动回调

NestedScrolling机制是这样的：内部View在滚动的时候，首先将dx,dy交给NestedScrollingParent，NestedScrollingParent可对其进行部分消耗，剩余的部分还给内部View。
