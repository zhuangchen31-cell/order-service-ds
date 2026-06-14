/**
 * promotion-tag.js — 促销标签子组件
 *
 * 功能：接收父组件传入的促销信息，渲染不同样式的促销标签
 *
 * 父→子传值接口：
 *   new PromotionTag({ type, label, discount, style })
 *
 * Props说明：
 *   - type: string   促销类型 (flash|new|fullreduction|coupon|special)
 *   - label: string  促销标签文字
 *   - discount: string  折扣/优惠信息
 *   - style: object  自定义样式覆盖（可选）
 */

const PromotionTag = (() => {
  // ========== 促销类型预设配置 ==========
  const TYPE_CONFIG = {
    flash: {
      className: 'promo-tag-flash',
      icon: '⚡',
      defaultLabel: '限时优惠',
      bgColor: '#ff4444',
      textColor: '#fff'
    },
    new: {
      className: 'promo-tag-new',
      icon: '🆕',
      defaultLabel: '新品上市',
      bgColor: '#409eff',
      textColor: '#fff'
    },
    fullreduction: {
      className: 'promo-tag-full',
      icon: '🎁',
      defaultLabel: '满减促销',
      bgColor: '#ff9800',
      textColor: '#fff'
    },
    coupon: {
      className: 'promo-tag-coupon',
      icon: '🎫',
      defaultLabel: '优惠券',
      bgColor: '#67c23a',
      textColor: '#fff'
    },
    special: {
      className: 'promo-tag-special',
      icon: '🔥',
      defaultLabel: '特价秒杀',
      bgColor: '#e040fb',
      textColor: '#fff'
    }
  };

  /**
   * PromotionTag 组件类
   * 父组件通过 new PromotionTag(props) 创建实例，调用 .render(container) 渲染
   */
  class PromotionTagComponent {
    /**
     * @param {Object} props — 父组件传入的促销数据
     * @param {string} props.type — 促销类型
     * @param {string} [props.label] — 自定义标签文字
     * @param {string} [props.discount] — 折扣信息
     * @param {Object} [props.style] — 自定义样式覆盖
     */
    constructor(props = {}) {
      this.props = {
        type: props.type || 'special',
        label: props.label || '',
        discount: props.discount || '',
        style: props.style || {}
      };

      this.config = TYPE_CONFIG[this.props.type] || TYPE_CONFIG.special;
      this.container = null;
      this.element = null;
    }

    /**
     * 渲染组件到指定容器
     * @param {HTMLElement|string} container — 父组件传入的容器元素或选择器
     * @returns {HTMLElement} 组件DOM元素
     */
    render(container) {
      this.container = typeof container === 'string'
        ? document.querySelector(container)
        : container;

      if (!this.container) {
        console.error('PromotionTag: 容器元素不存在');
        return null;
      }

      const cfg = this.config;
      const label = this.props.label || cfg.defaultLabel;
      const discount = this.props.discount;
      const customStyle = this.props.style;

      // 构建内联样式
      const inlineStyle = `
        background: ${customStyle.bgColor || cfg.bgColor};
        color: ${customStyle.textColor || cfg.textColor};
        ${customStyle.css || ''}
      `;

      this.element = document.createElement('span');
      this.element.className = `promotion-tag ${cfg.className}`;
      this.element.setAttribute('style', inlineStyle);
      this.element.setAttribute('data-promo-type', this.props.type);
      this.element.innerHTML = `
        <span class="promo-icon">${cfg.icon}</span>
        <span class="promo-label">${this._escapeHtml(label)}</span>
        ${discount ? `<span class="promo-discount">${this._escapeHtml(discount)}</span>` : ''}
      `;

      this.container.appendChild(this.element);
      return this.element;
    }

    /**
     * 更新促销数据（父组件可动态修改）
     * @param {Object} newProps — 新的促销数据
     */
    update(newProps = {}) {
      Object.assign(this.props, newProps);
      if (newProps.type) {
        this.config = TYPE_CONFIG[newProps.type] || TYPE_CONFIG.special;
      }

      if (this.element) {
        const cfg = this.config;
        const label = this.props.label || cfg.defaultLabel;
        const discount = this.props.discount;

        this.element.className = `promotion-tag ${cfg.className}`;
        this.element.setAttribute('data-promo-type', this.props.type);
        this.element.style.background = (this.props.style.bgColor || cfg.bgColor);
        this.element.style.color = (this.props.style.textColor || cfg.textColor);
        this.element.innerHTML = `
          <span class="promo-icon">${cfg.icon}</span>
          <span class="promo-label">${this._escapeHtml(label)}</span>
          ${discount ? `<span class="promo-discount">${this._escapeHtml(discount)}</span>` : ''}
        `;
      }
    }

    /**
     * 移除组件DOM
     */
    destroy() {
      if (this.element && this.element.parentNode) {
        this.element.parentNode.removeChild(this.element);
        this.element = null;
      }
    }

    /**
     * 获取组件DOM元素
     * @returns {HTMLElement|null}
     */
    getElement() {
      return this.element;
    }

    // XSS防护
    _escapeHtml(str) {
      if (!str) return '';
      const div = document.createElement('div');
      div.textContent = str;
      return div.innerHTML;
    }
  }

  // ========== 公开接口（工厂函数） ==========
  /**
   * 创建促销标签实例（推荐使用此方法）
   * @param {Object} props — 父组件传入的促销数据
   * @returns {PromotionTagComponent}
   */
  function create(props) {
    return new PromotionTagComponent(props);
  }

  return {
    create,
    PromotionTagComponent,
    TYPE_CONFIG
  };
})();

// 导出
if (typeof module !== 'undefined' && module.exports) {
  module.exports = PromotionTag;
}
