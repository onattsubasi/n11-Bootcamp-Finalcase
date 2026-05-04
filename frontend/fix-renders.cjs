const fs = require('fs');
const path = require('path');

const replacements = [
  {
    file: 'src/pages/ProductDetail.jsx',
    search: '{!lists?.length && <div className="p-4 text-xs text-gray-400">No lists found</div>}',
    replace: '{!lists?.length ? <div className="p-4 text-xs text-gray-400">No lists found</div> : null}'
  },
  {
    file: 'src/pages/CheckoutPage.jsx',
    search: "{!addresses?.length && <p className='text-gray-500 italic'>No addresses found. Add one in profile.</p>}",
    replace: "{!addresses?.length ? <p className='text-gray-500 italic'>No addresses found. Add one in profile.</p> : null}"
  },
  {
    file: 'src/pages/AdminUsers.jsx',
    search: '{!users.length && <div className="p-10 text-center text-gray-500">No users found.</div>}',
    replace: '{!users.length ? <div className="p-10 text-center text-gray-500">No users found.</div> : null}'
  },
  {
    file: 'src/pages/AdminPromotions.jsx',
    search: '{!coupons?.length && <div className="p-10 text-center text-gray-500">No coupons found. Generate a batch or add one.</div>}',
    replace: '{!coupons?.length ? <div className="p-10 text-center text-gray-500">No coupons found. Generate a batch or add one.</div> : null}'
  },
  {
    file: 'src/pages/AdminInventory.jsx',
    search: '{item.productImageUrl && <img src={item.productImageUrl} className="h-full w-full object-cover" />}',
    replace: '{item.productImageUrl ? <img src={item.productImageUrl} className="h-full w-full object-cover" /> : null}'
  },
  {
    file: 'src/pages/AdminCheckouts.jsx',
    search: "{!checkouts.length && <div className='p-10 text-center text-gray-500'>No checkout sessions found.</div>}",
    replace: "{!checkouts.length ? <div className='p-10 text-center text-gray-500'>No checkout sessions found.</div> : null}"
  },
  {
    file: 'src/features/profile/components/ProductListsTab.jsx',
    search: '{!lists?.length && !isCreating && <p className="text-sm text-gray-400 text-center py-4">No lists yet.</p>}',
    replace: '{!lists?.length && !isCreating ? <p className="text-sm text-gray-400 text-center py-4">No lists yet.</p> : null}'
  },
  {
    file: 'src/features/profile/components/ProductListsTab.jsx',
    search: '{item.productImageUrl && <img src={item.productImageUrl} className="h-full w-full object-cover" />}',
    replace: '{item.productImageUrl ? <img src={item.productImageUrl} className="h-full w-full object-cover" /> : null}'
  },
  {
    file: 'src/components/layout/Navbar.jsx',
    search: '{item.category && <span className="text-[10px] bg-gray-100 px-1.5 py-0.5 rounded text-gray-500">{item.category}</span>}',
    replace: '{item.category ? <span className="text-[10px] bg-gray-100 px-1.5 py-0.5 rounded text-gray-500">{item.category}</span> : null}'
  }
];

replacements.forEach(({ file, search, replace }) => {
  const filePath = path.join(__dirname, file);
  if (fs.existsSync(filePath)) {
    let content = fs.readFileSync(filePath, 'utf8');
    if (content.includes(search)) {
      content = content.replace(search, replace);
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`Fixed ${file}`);
    } else {
      console.log(`Search string not found in ${file}`);
    }
  } else {
    console.log(`File not found: ${file}`);
  }
});
