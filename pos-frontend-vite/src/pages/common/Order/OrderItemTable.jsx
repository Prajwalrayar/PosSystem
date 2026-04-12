import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "../../../components/ui/table";

const OrderItemTable = ({ selectedOrder }) => {
  return (
    <Table className="w-full table-fixed">
      <TableHeader>
        <TableRow>
          <TableHead className="w-12 px-2">Image</TableHead>
          <TableHead className="px-2">Item</TableHead>
          <TableHead className="w-14 px-2 text-center">Qty</TableHead>
          <TableHead className="w-24 px-2 text-right">Price</TableHead>
          <TableHead className="w-24 px-2 text-right">Total</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {selectedOrder.items?.map((item) => (
          <TableRow key={item.id} className="text-xs sm:text-sm">
            <TableCell className="px-2 py-2">
              <div className="h-8 w-8 sm:h-9 sm:w-9">
                {item.product?.image ? (
                  <img
                    src={item.product.image}
                    alt={item.productName || item.product?.name || "Product"}
                    className="h-8 w-8 rounded object-cover sm:h-9 sm:w-9"
                  />
                ) : null}
                {(!item.product?.image || item.product?.image === "") && (
                  <div className="flex h-8 w-8 items-center justify-center rounded border bg-gray-100 sm:h-9 sm:w-9">
                    <span className="text-xs text-gray-500 font-medium">
                      {item.productName
                        ? item.productName.charAt(0).toUpperCase()
                        : item.product?.name
                        ? item.product.name.charAt(0).toUpperCase()
                        : "P"}
                    </span>
                  </div>
                )}
              </div>
            </TableCell>
            <TableCell className="px-2 py-2">
              <div className="flex max-w-[180px] flex-col sm:max-w-[220px]">
                <span className="font-medium truncate">
                  {item.product?.name || item.productName || "Product"}
                </span>
                {item.product?.sku && (
                  <span className="truncate text-[11px] text-gray-500">
                    SKU: {item.product.sku}
                  </span>
                )}
              </div>
            </TableCell>
            <TableCell className="px-2 py-2 text-center">{item.quantity}</TableCell>
            <TableCell className="px-2 py-2 text-right">
              ₹{item.product?.sellingPrice?.toFixed(2) || "0.00"}
            </TableCell>
            <TableCell className="px-2 py-2 text-right">
              ₹
              {(item.product?.sellingPrice * item.quantity)?.toFixed(2) ||
                "0.00"}
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
};

export default OrderItemTable;
