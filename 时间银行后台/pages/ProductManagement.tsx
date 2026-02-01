import React, { useState, useEffect } from 'react';
import { productApi } from '../services/productApi';
import { Product } from '../types';
import { Plus, Coins, Edit2, Trash2 } from 'lucide-react';
import { Modal } from '../components/ui/Modal';

export const ProductManagement: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentProduct, setCurrentProduct] = useState<Partial<Product>>({});

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const data = await productApi.getProducts();
      setProducts(data);
    } catch (error) {
      console.error('Failed to fetch products', error);
      alert('获取商品列表失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const handleOpenAdd = () => {
    setCurrentProduct({});
    setIsModalOpen(true);
  };

  const handleOpenEdit = (product: Product) => {
    setCurrentProduct(product);
    setIsModalOpen(true);
  };

  const handleSave = async () => {
    // Basic validation
    if (!currentProduct.name || !currentProduct.price) {
      alert('请填写必填项');
      return;
    }

    try {
      if (currentProduct.id) {
        // Edit
        await productApi.updateProduct(currentProduct);
      } else {
        // Add
        await productApi.createProduct({
          ...currentProduct,
          status: 'ON_SHELF',
          salesCount: 0,
          stock: currentProduct.stock || 0
        });
      }
      setIsModalOpen(false);
      fetchProducts(); // Refresh list
    } catch (error) {
      console.error('Save failed', error);
      alert('保存失败');
    }
  };

  const toggleStatus = async (id: string, currentStatus: string) => {
    try {
      const newStatus = currentStatus === 'ON_SHELF' ? 'OFF_SHELF' : 'ON_SHELF';
      await productApi.updateProductStatus(id, newStatus);
      fetchProducts();
    } catch (error) {
      console.error('Status update failed', error);
      alert('状态更新失败');
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm('确定要删除该商品吗？')) return;
    try {
      await productApi.deleteProduct(id);
      fetchProducts();
    } catch (error) {
      console.error('Delete failed', error);
      alert('删除失败');
    }
  };

  return (
    <div className="space-y-6">
      {/* Toolbar */}
      <div className="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm border border-gray-100">
        <h2 className="text-lg font-medium text-gray-900">商品列表</h2>
        <button
          onClick={handleOpenAdd}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
        >
          <Plus className="w-5 h-5 mr-2" />
          新增商品
        </button>
      </div>

      {/* Product Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-500">加载中...</div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商品信息</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">分类</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">兑换价格</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">库存</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">已兑换</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {products.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-500">
                    暂无商品数据
                  </td>
                </tr>
              ) : (
                products.map((product) => (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-16 w-16">
                          <img className="h-16 w-16 rounded object-cover" src={product.image || 'https://via.placeholder.com/150'} alt="" />
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{product.name}</div>
                          <div className="text-xs text-gray-500 text-ellipsis overflow-hidden max-w-xs">{product.description}</div>
                          <div className="text-xs text-gray-400 font-mono mt-1">ID: {product.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {product.category || '-'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center text-sm font-bold text-orange-600">
                        <Coins className="w-4 h-4 mr-1 text-orange-500" />
                        {product.price}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`text-sm font-medium ${product.stock < 10 ? 'text-red-600' : 'text-gray-900'}`}>
                        {product.stock}
                      </span>
                      {product.stock < 10 && <span className="ml-2 text-xs text-red-500 bg-red-50 px-1 rounded">库存紧张</span>}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {product.salesCount} 次
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        onClick={() => toggleStatus(product.id, product.status)}
                        className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none ${product.status === 'ON_SHELF' ? 'bg-blue-600' : 'bg-gray-200'}`}
                      >
                        <span className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${product.status === 'ON_SHELF' ? 'translate-x-5' : 'translate-x-0'}`} />
                      </button>
                      <span className="ml-2 text-xs text-gray-500">{product.status === 'ON_SHELF' ? '已上架' : '已下架'}</span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button onClick={() => handleOpenEdit(product)} className="text-blue-600 hover:text-blue-900 mr-4">
                        <Edit2 className="w-5 h-5" />
                      </button>
                      <button onClick={() => handleDelete(product.id)} className="text-red-600 hover:text-red-900">
                        <Trash2 className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Edit/Add Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={currentProduct.id ? '编辑商品' : '新增商品'}
      >
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">商品名称</label>
            <input
              type="text"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.name || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, name: e.target.value })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">商品描述</label>
            <textarea
              rows={3}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.description || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, description: e.target.value })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">兑换所需积分</label>
            <input
              type="number"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.price || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, price: parseInt(e.target.value) })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">库存数量</label>
            <input
              type="number"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.stock || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, stock: parseInt(e.target.value) })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">商品图片 URL</label>
            <input
              type="text"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.image || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, image: e.target.value })}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">商品分类</label>
            <select
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              value={currentProduct.category || ''}
              onChange={e => setCurrentProduct({ ...currentProduct, category: e.target.value as any })}
            >
              <option value="">请选择分类</option>
              <option value="粮油副食">粮油副食</option>
              <option value="日用百货">日用百货</option>
              <option value="医疗健康">医疗健康</option>
              <option value="虚拟券卡">虚拟券卡</option>
            </select>
          </div>

          <div className="flex justify-end gap-3 mt-6 pt-4 border-t">
            <button onClick={() => setIsModalOpen(false)} className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50">取消</button>
            <button onClick={handleSave} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">保存</button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
