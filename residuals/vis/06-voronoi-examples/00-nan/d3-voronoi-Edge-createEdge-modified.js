function createEdge(left, right, v0, v1) {
  var edge = [null, null];
  var index = edges.length;
      // index = edges.push(edge) - 1;
  edges.push(edge);
  edge.left = left;
  edge.right = right;
  if (v0) setEdgeEnd(edge, left, right, v0);
  if (v1) setEdgeEnd(edge, right, left, v1);
  cells[left.index].halfedges.push(index);
  cells[right.index].halfedges.push(index);
  return edge;
}