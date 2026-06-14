/**
 * countdown-timer.js — 倒计时子组件
 *
 * 功能：接收父组件传入的结束时间，实现实时倒计时显示
 *
 * 父→子传值接口：
 *   new CountdownTimer({ endTime, onExpire, onTick, showLabels })
 *
 * Props说明：
 *   - endTime: string|number  ISO时间字符串 或 毫秒时间戳（父组件必传）
 *   - onExpire: function  倒计时到期回调（可选）
 *   - onTick: function    每秒滴答回调，传入剩余时间对象（可选）
 *   - showLabels: boolean  是否显示"天/时/分/秒"标签（默认true）
 *   - compact: boolean     紧凑模式（默认false）
 *   - className: string    自定义CSS类名（可选）
 */

const CountdownTimer = (() => {
  class CountdownTimerComponent {
    /**
     * @param {Object} props — 父组件传入的配置
     * @param {string|number} props.endTime — 促销结束时间（ISO字符串或毫秒时间戳）
     * @param {Function} [props.onExpire] — 到期回调
     * @param {Function} [props.onTick] — 每秒回调 (remaining) => {}
     * @param {boolean} [props.showLabels] — 是否显示单位标签，默认true
     * @param {boolean} [props.compact] — 紧凑模式
     * @param {string} [props.className] — 自定义类名
     */
    constructor(props = {}) {
      // 父组件传入的结束时间（必传）
      this.endTime = this._parseEndTime(props.endTime);
      this.onExpire = props.onExpire || null;
      this.onTick = props.onTick || null;
      this.showLabels = props.showLabels !== undefined ? props.showLabels : true;
      this.compact = props.compact || false;
      this.customClass = props.className || '';

      // 内部状态
      this.container = null;
      this.element = null;
      this.timerId = null;
      this.isExpired = false;
      this.remaining = { days: 0, hours: 0, minutes: 0, seconds: 0, total: 0 };
    }

    /**
     * 渲染倒计时到指定容器
     * @param {HTMLElement|string} container — 父组件传入的容器
     * @returns {HTMLElement}
     */
    render(container) {
      this.container = typeof container === 'string'
        ? document.querySelector(container)
        : container;

      if (!this.container) {
        console.error('CountdownTimer: 容器元素不存在');
        return null;
      }

      // 构建DOM结构
      const compactClass = this.compact ? 'countdown-compact' : '';
      this.element = document.createElement('div');
      this.element.className = `countdown-timer ${compactClass} ${this.customClass}`.trim();

      this.element.innerHTML = `
        <div class="countdown-block">
          <span class="countdown-num" id="cd-days">00</span>
          ${this.showLabels ? '<span class="countdown-label">天</span>' : ''}
        </div>
        <span class="countdown-sep">:</span>
        <div class="countdown-block">
          <span class="countdown-num" id="cd-hours">00</span>
          ${this.showLabels ? '<span class="countdown-label">时</span>' : ''}
        </div>
        <span class="countdown-sep">:</span>
        <div class="countdown-block">
          <span class="countdown-num" id="cd-minutes">00</span>
          ${this.showLabels ? '<span class="countdown-label">分</span>' : ''}
        </div>
        <span class="countdown-sep">:</span>
        <div class="countdown-block">
          <span class="countdown-num" id="cd-seconds">00</span>
          ${this.showLabels ? '<span class="countdown-label">秒</span>' : ''}
        </div>
      `;

      this.container.appendChild(this.element);

      // 缓存数字元素引用
      this._daysEl = this.element.querySelector('#cd-days');
      this._hoursEl = this.element.querySelector('#cd-hours');
      this._minutesEl = this.element.querySelector('#cd-minutes');
      this._secondsEl = this.element.querySelector('#cd-seconds');

      // 初始化显示
      this._tick();
      // 启动定时器
      this._start();

      return this.element;
    }

    /**
     * 更新结束时间（父组件可动态修改）
     * @param {string|number} newEndTime
     */
    updateEndTime(newEndTime) {
      this.endTime = this._parseEndTime(newEndTime);
      this.isExpired = false;
      this._tick();
      this._start();
    }

    /**
     * 手动停止倒计时
     */
    stop() {
      this._clearTimer();
    }

    /**
     * 销毁组件（清除定时器 + 移除DOM）
     */
    destroy() {
      this._clearTimer();
      if (this.element && this.element.parentNode) {
        this.element.parentNode.removeChild(this.element);
        this.element = null;
      }
    }

    /**
     * 获取当前剩余时间
     * @returns {Object} { days, hours, minutes, seconds, total }
     */
    getRemaining() {
      return { ...this.remaining };
    }

    /**
     * 检查是否已到期
     * @returns {boolean}
     */
    hasExpired() {
      return this.isExpired;
    }

    // ========== 私有方法 ==========

    _parseEndTime(endTime) {
      if (!endTime) {
        // 默认1小时后到期
        return Date.now() + 3600000;
      }
      if (typeof endTime === 'number') return endTime;
      const ts = new Date(endTime).getTime();
      return isNaN(ts) ? Date.now() + 3600000 : ts;
    }

    _start() {
      this._clearTimer();
      this.timerId = setInterval(() => this._tick(), 1000);
    }

    _clearTimer() {
      if (this.timerId) {
        clearInterval(this.timerId);
        this.timerId = null;
      }
    }

    _tick() {
      const now = Date.now();
      let total = Math.max(0, Math.floor((this.endTime - now) / 1000));

      if (total <= 0) {
        // 倒计时到期
        this._clearTimer();
        this.isExpired = true;
        this.remaining = { days: 0, hours: 0, minutes: 0, seconds: 0, total: 0 };
        this._updateDisplay();
        if (typeof this.onExpire === 'function') {
          this.onExpire();
        }
        return;
      }

      const days = Math.floor(total / 86400);
      const hours = Math.floor((total % 86400) / 3600);
      const minutes = Math.floor((total % 3600) / 60);
      const seconds = total % 60;

      this.remaining = { days, hours, minutes, seconds, total };
      this._updateDisplay();

      if (typeof this.onTick === 'function') {
        this.onTick({ ...this.remaining });
      }
    }

    _updateDisplay() {
      const pad = (n) => String(n).padStart(2, '0');
      if (this._daysEl) this._daysEl.textContent = pad(this.remaining.days);
      if (this._hoursEl) this._hoursEl.textContent = pad(this.remaining.hours);
      if (this._minutesEl) this._minutesEl.textContent = pad(this.remaining.minutes);
      if (this._secondsEl) this._secondsEl.textContent = pad(this.remaining.seconds);

      // 到期样式
      if (this.isExpired && this.element) {
        this.element.classList.add('countdown-expired');
      }
    }
  }

  // ========== 公开接口 ==========
  function create(props) {
    return new CountdownTimerComponent(props);
  }

  return {
    create,
    CountdownTimerComponent
  };
})();

// 导出
if (typeof module !== 'undefined' && module.exports) {
  module.exports = CountdownTimer;
}
